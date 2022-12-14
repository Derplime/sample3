(ns sample3.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample3.ajax :as ajax]
    [sample3.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [ajax.core :refer [GET POST]])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample3"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(comment

  (def app-data (r/atom {:x 0 :y 0 :total 0}))

  (defn swap [val]
        (swap! app-data assoc
               :total val)
        (js/console.log "The value from plus API is" (str (:total @app-data)))); Value comes out in console

  (defn math [params operation]
        (POST (str "/api/math/" operation)
              {:headers {"accept" "application/transit-json"}
               :params  @params
               :handler #(swap (:total %))}))

  (defn getAdd []
        (GET "/api/math/plus?x=1&y=2"
             {:headers {"accept" "application/json"}
              :handler #(swap (:total %))}))

  ; TODO - update to clojure parseInt
  (defn int-value [v]
        (-> v .-target .-value int))

  ; Function to update the color of the p tag class
  (defn change-color []
        (cond
          (<= 0 (:total @app-data) 19) {:style {:color "lightgreen" :font-weight :bold}}
          (<= 20 (:total @app-data) 49) {:style {:color "lightblue" :font-weight :bold}}
          :default {:style {:color "lightsalmon" :font-weight :bold}}))

  (:total @app-data)
  (POST "/api/math/plus"
        {:headers {"accept" "application/transit-json"}
         :params  {:x 1 :y 2}
         :handler #(swap (:total %))})

  (<= 0 34 48)

  (defn home-page-sample1 []
        (let [params (r/atom {})]
             [:section.section>div.container>div.content
              [:h1 "Hello World!"]
              [:h3 "Button to add 1 + 2. Result in text at the bottom."]
              [:button.button.is-primary {:on-click #(getAdd)} "1 + 2"]
              [:h3 "Fill out fields and select the desired operation. Result in text at the bottom."]
              [:form
               [:div.form-group
                [:label "Value 1: "]
                [:input {:type :text :placeholder "0" :on-change #(swap! params assoc :x (int-value %))}]]
               [:div.form-group
                [:label "Value 2: "]
                [:input {:type :text :placeholder "0" :on-change #(swap! params assoc :y (int-value %))}]]]
              [:br]
              [:button.button.is-primary {:on-click #(math params "plus")} "+"]
              [:button.button.is-info {:on-click #(math params "minus")} "-"]
              [:button.button.is-warning {:on-click #(math params "multiply")} "*"]
              [:button.button.is-danger {:on-click #(math params "divide")} "/"]
              [:br]
              [:br]
              [:p "Your calculated value is: "
               [:span (change-color) (:total @app-data)]] ; update the class here
              ]))
  )

; TODO - update to clojure parseInt
(defn int-value [v]
      (-> v .-target .-value int))

(defn math [operation]
      (let [xVal @(rf/subscribe [:xVal])
            yVal @(rf/subscribe [:yVal])]
      (POST (str "/api/math/" operation)
            {:headers {"accept" "application/transit-json"}
             :params  {:x xVal :y yVal}
             :handler #(rf/dispatch [:set-total (:total %)])})))

(defn getAdd []
      (GET "/api/math/plus?x=1&y=2"
           {:headers {"accept" "application/json"}
            :handler #(rf/dispatch [:set-total (:total %)])}))

; Function to update the color of the p tag class
(defn change-color []
      (let [total @(rf/subscribe [:total])]
      (cond
        (<= 0 total 19) {:style {:color "lightgreen" :font-weight :bold}}
        (<= 20 total 49) {:style {:color "lightblue" :font-weight :bold}}
        :default {:style {:color "lightsalmon" :font-weight :bold}})))

(defn home-page []
  (let [total @(rf/subscribe [:total])
        params (r/atom {})]
  [:section.section>div.container>div.content
   [:h1 "Hello World! I'm Sample 3!"]
   [:h3 "The button below will compute the result of 1 + 2. The result will appear at the bottom."]
   [:button.button.is-primary {:on-click #(getAdd)} "1 + 2"]
   [:h3 "Fill out fields and select the desired operation. The result will appear at the bottom."]
   [:h3 "The computation will be read as (Value 1) (Operator) (Value 2)"]
   [:form
    [:div.form-group
     [:label "Value 1: "]
     [:input {:type :text :placeholder "0" :on-change #(rf/dispatch [:set-xVal (int-value %)])}]]
    [:div.form-group
     [:label "Value 2: "]
     [:input {:type :text :placeholder "0" :on-change #(rf/dispatch [:set-yVal (int-value %)])}]]]
   [:br]
   [:button.button.is-primary {:on-click #(math "plus")} "+"]
   [:button.button.is-info {:on-click #(math "minus")} "-"]
   [:button.button.is-warning {:on-click #(math "multiply")} "*"]
   [:button.button.is-danger {:on-click #(math "divide")} "/"]
   [:br]
   [:br]
   [:div "Your calculated result is: "
    [:span (change-color) total]]
   ]))

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
