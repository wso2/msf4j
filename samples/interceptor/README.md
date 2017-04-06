#Applying interceptors

* Please refer [MSF4J Interceptors - Common](../interceptor/interceptor-common) for writing request and response interceptors
* Please refer [MSF4J Interceptor Service - Fat Jar mode](../interceptor/fatjar-interceptor-service) for using interceptors in fat jar mode
* Please refer [MSF4J Interceptor Service - Deployable Jar mode](../interceptor/deployable-jar-interceptor-service) for using interceptors in deployable jar mode
* Please refer [MSF4J Interceptor Service - OSGi mode](../interceptor/osgi-interceptor-service) for using interceptors in OSGi mode

# Priority order of Interceptors

## General order of interceptors
1) Global request interceptors
2) Class level annotated request interceptors
3) Method level annotated request interceptors
4) Resource method execution
5) Sub-resource class level annotated request interceptors
6) Sub-resource method level annotated request interceptors
7) Sub-resource execution
8) Sub-resource method level annotated response interceptors
9) Sub-resource class level annotated response interceptors
10) Method level annotated response interceptors
11) Class level annotated response interceptors
12) Global response interceptors

Priority of the interceptors in the global level will be exactly the order in which
they are registered (order in which they are written)


Priority for resource / sub-resource interceptors will be the order in which they are
written inside the @RequestInterceptor / @ResponseInterceptor annotation.

# Special notes
### Why not use JAX-RS @NameBinding ?

* The performance hit in fat jar at the service start time would be significant (when scanning request and response interceptors at the start time)
* The developer has to write annotations for each different request or response filter he/she introduces.
* Not fail safe when introducing global interceptors (humans are error prone)