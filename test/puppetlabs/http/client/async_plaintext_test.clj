(ns puppetlabs.http.client.async-plaintext-test
  (:import (com.puppetlabs.http.client AsyncHttpClient RequestOptions)
           (org.apache.http.impl.nio.client HttpAsyncClients)
           (java.net URI))
  (:require [clojure.test :refer :all]
            [puppetlabs.http.client.test-common :refer :all]
            [puppetlabs.trapperkeeper.core :as tk]
            [puppetlabs.trapperkeeper.testutils.bootstrap :as testutils]
            [puppetlabs.trapperkeeper.testutils.logging :as testlogging]
            [puppetlabs.trapperkeeper.services.webserver.jetty9-service :as jetty9]
            [puppetlabs.http.client.common :as common]
            [puppetlabs.http.client.async :as async]
            [schema.test :as schema-test]))

(use-fixtures :once schema-test/validate-schemas)

(defn app
  [req]
  {:status 200
   :body "Hello, World!"})

(tk/defservice test-web-service
  [[:WebserverService add-ring-handler]]
  (init [this context]
        (add-ring-handler app "/hello")
        context))

(defn basic-test
  [http-method java-method clj-fn]
  (testing (format "async client: HTTP method: '%s'" http-method)
    (testlogging/with-test-logging
      (testutils/with-app-with-config app
        [jetty9/jetty9-service test-web-service]
        {:webserver {:port 10000}}
        (testing "java async client"
          (let [options (RequestOptions. "http://localhost:10000/hello/")
                response (java-method options)]
            (is (= 200 (.getStatus (.deref response))))
            (is (= "Hello, World!" (slurp (.getBody (.deref response)))))))
        (testing "clojure async client"
          (let [response (clj-fn "http://localhost:10000/hello/")]
            (is (= 200 (:status @response)))
            (is (= "Hello, World!" (slurp (:body @response))))))))))

(deftest async-client-head-test
  (testlogging/with-test-logging
    (testutils/with-app-with-config app
      [jetty9/jetty9-service test-web-service]
      {:webserver {:port 10000}}
      (testing "java async client"
        (let [options (RequestOptions. (URI. "http://localhost:10000/hello/"))
              response (AsyncHttpClient/head options)]
          (is (= 200 (.getStatus (.deref response))))
          (is (= nil (.getBody (.deref response))))))
      (testing "clojure sync client"
        (let [response (async/head "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= nil (:body @response))))))))

(deftest async-client-get-test
  (basic-test "GET" #(AsyncHttpClient/get %) async/get))

(deftest async-client-post-test
  (basic-test "POST" #(AsyncHttpClient/post %) async/post))

(deftest async-client-put-test
  (basic-test "PUT" #(AsyncHttpClient/put %) async/put))

(deftest async-client-delete-test
  (basic-test "DELETE" #(AsyncHttpClient/delete %) async/delete))

(deftest async-client-trace-test
  (basic-test "TRACE" #(AsyncHttpClient/trace %) async/trace))

(deftest async-client-options-test
  (basic-test "OPTIONS" #(AsyncHttpClient/options %) async/options))

(deftest async-client-patch-test
  (basic-test "PATCH" #(AsyncHttpClient/patch %) async/patch))

(deftest persistent-async-client-test
  (testlogging/with-test-logging
    (testutils/with-app-with-config app
    [jetty9/jetty9-service test-web-service]
    {:webserver {:port 10000}}
    (let [client (async/create-client {})]
      (testing "HEAD request with persistent async client"
        (let [response (common/head client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= nil (:body @response)))))
      (testing "GET request with persistent async client"
        (let [response (common/get client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "POST request with persistent async client"
        (let [response (common/post client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "PUT request with persistent async client"
        (let [response (common/put client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "DELETE request with persistent async client"
        (let [response (common/delete client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "TRACE request with persistent async client"
        (let [response (common/trace client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "OPTIONS request with persistent async client"
        (let [response (common/options client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "PATCH request with persistent async client"
        (let [response (common/patch client "http://localhost:10000/hello/")]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (slurp (:body @response))))))
      (testing "client closes properly"
        (common/close client)
        (is (thrown? IllegalStateException (common/get client "http://localhost:10000/hello/"))))))))

(deftest request-with-client-test
  (testlogging/with-test-logging
    (testutils/with-app-with-config app
      [jetty9/jetty9-service test-web-service]
      {:webserver {:port 10000}}
      (let [client (HttpAsyncClients/createDefault)
            opts   {:method :get :url "http://localhost:10000/hello/"}]
        (.start client)
        (testing "GET request works with request-with-client"
          (let [response (async/request-with-client opts nil client)]
            (is (= 200 (:status @response)))
            (is (= "Hello, World!" (slurp (:body @response))))))
        (testing "Client persists when passed to request-with-client"
          (let [response (async/request-with-client opts nil client)]
            (is (= 200 (:status @response)))
            (is (= "Hello, World!" (slurp (:body @response))))))
        (.close client)))))

(deftest query-params-test-async
  (testlogging/with-test-logging
    (testutils/with-app-with-config app
      [jetty9/jetty9-service test-params-web-service]
      {:webserver {:port 8080}}
      (testing "URL Query Parameters work with the Java client"
        (let [options (RequestOptions. (URI. "http://localhost:8080/params?foo=bar&baz=lux"))]
          (let [response (AsyncHttpClient/get options)]
            (is (= 200 (.getStatus (.deref response))))
            (is (= queryparams (read-string (slurp (.getBody (.deref response)))))))))

      (testing "URL Query Parameters work with the clojure client"
        (let [opts {:method       :get
                    :url          "http://localhost:8080/params/"
                    :query-params queryparams
                    :as           :text}]
          (let [response (async/get "http://localhost:8080/params" opts)]
            (is (= 200 (:status @response)))
            (is (= queryparams (read-string (:body @response)))))))

      (testing "URL Query Parameters can be set directly in the URL"
        (let [response (async/get "http://localhost:8080/params?paramone=one"
                                  {:as :text})]
          (is (= 200 (:status @response)))
          (is (= (str {"paramone" "one"}) (:body @response)))))

      (testing (str "URL Query Parameters set in URL are overwritten if params "
                    "are also specified in options map")
        (let [response (async/get "http://localhost:8080/params?paramone=one&foo=lux"
                                  query-options)]
          (is (= 200 (:status @response)))
          (is (= queryparams (read-string (:body @response)))))))))

(deftest redirect-test-async
  (testlogging/with-test-logging
    (testutils/with-app-with-config app
      [jetty9/jetty9-service redirect-web-service]
      {:webserver {:port 8080}}
      (testing (str "redirects on POST not followed by Java client "
                    "when forceRedirects option not set to true")
        (let [options  (RequestOptions. (URI. "http://localhost:8080/hello"))
              response (AsyncHttpClient/post options)]
          (is (= 302 (.getStatus (.deref response))))))
      (testing "redirects on POST followed by Java client when option is set"
        (let [options (.. (RequestOptions. (URI. "http://localhost:8080/hello"))
                          (setForceRedirects true))
              response (AsyncHttpClient/post options)]
          (is (= 200 (.getStatus (.deref response))))
          (is (= "Hello, World!" (slurp (.getBody (.deref response)))))))
      (testing "redirects not followed by Java client when :follow-redirects is false"
        (let [options (.. (RequestOptions. (URI. "http://localhost:8080/hello"))
                          (setFollowRedirects false))
              response (AsyncHttpClient/get options)]
          (is (= 302 (.getStatus (.deref response))))))
      (testing ":follow-redirects overrides :force-redirects for Java client"
        (let [options (.. (RequestOptions. (URI. "http://localhost:8080/hello"))
                          (setFollowRedirects false)
                          (setForceRedirects true))
              response (AsyncHttpClient/get options)]
          (is (= 302 (.getStatus (.deref response))))))
      (testing (str "redirects on POST not followed by clojure client "
                    "when :force-redirects is not set to true")
        (let [opts     {:method           :post
                        :url              "http://localhost:8080/hello"
                        :as               :text
                        :force-redirects  false}
              response (async/post "http://localhost:8080/hello" opts)]
          (is (= 302 (:status @response)))))
      (testing "redirects on POST followed by clojure client when option is set"
        (let [opts     {:method           :post
                        :url              "http://localhost:8080/hello"
                        :as               :text
                        :force-redirects  true}
              response (async/post "http://localhost:8080/hello" opts)]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (:body @response)))))
      (testing (str "redirects not followed by clojure client when :follow-redirects "
                    "is set to false")
        (let [response (async/get "http://localhost:8080/hello" {:as :text
                                                                 :follow-redirects false})]
          (is (= 302 (:status @response)))))
      (testing ":follow-redirects overrides :force-redirects with clojure client"
        (let [response (async/get "http://localhost:8080/hello" {:as :text
                                                                 :follow-redirects false
                                                                 :force-redirects true})]
          (is (= 302 (:status @response)))))
      (testing (str "redirects on POST followed by persistent clojure client "
                    "when option is set")
        (let [client (async/create-client {:force-redirects true})
              response (common/post client "http://localhost:8080/hello" {:as :text})]
          (is (= 200 (:status @response)))
          (is (= "Hello, World!" (:body @response)))
          (common/close client)))
      (testing (str "persistent clojure client does not follow redirects when "
                    ":follow-redirects is set to false")
        (let [client (async/create-client {:follow-redirects false})
              response (common/get client "http://localhost:8080/hello" {:as :text})]
          (is (= 302 (:status @response)))
          (common/close client)))
      (testing ":follow-redirects overrides :force-redirects with persistent clj client"
        (let [client (async/create-client {:follow-redirects false
                                           :force-redirects true})
              response (common/get client "http://localhost:8080/hello" {:as :text})]
          (is (= 302 (:status @response)))
          (common/close client))))))
