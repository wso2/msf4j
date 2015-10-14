## Reliable, Scalable Redis on Kubernetes

The following document describes the deployment of a reliable, multi-node Redis on Kubernetes.  It deploys a master with replicated slaves, as well as replicated redis sentinels which are use for health checking and failover.


A [_Pod_](../../docs/user-guide/pods.md) is one or more containers that _must_ be scheduled onto the same host.  All containers in a pod share a network namespace, and may optionally share mounted volumes.

We will used the shared network namespace to bootstrap our Redis cluster.  In particular, the very first sentinel needs to know how to find the master (subsequent sentinels just ask the first sentinel).  Because all containers in a Pod share a network namespace, the sentinel can simply look at ```$(hostname -i):6379```.


Create this master as follows:

```sh
kubectl create -f redis-master.yaml
```

In Redis, we will use a Kubernetes Service to provide a discoverable endpoints for the Redis sentinels in the cluster.  From the sentinels Redis clients can find the master, and then the slaves and other relevant info for the cluster.  This enables new members to join the cluster when failures occur.

Create this service:

```sh
kubectl create -f redis-sentinel-service.yaml
```

Creating replication controllers for both master and the sentinel

```sh
kubectl create -f examples/redis/redis-controller.yaml
kubectl create -f examples/redis/redis-sentinel-controller.yaml
```

Initially creating those pods didn't actually do anything, since we only asked for one sentinel and one redis server, and they already existed, nothing changed.  Now we will add more replicas:

```sh
kubectl scale rc redis --replicas=3
```

```sh
kubectl scale rc redis-sentinel --replicas=3
```

This will create two additional replicas of the redis server and two additional replicas of the redis sentinel.

Unlike our original redis-master pod, these pods exist independently, and they use the ```redis-sentinel-service``` that we defined above to discover and join the cluster.

The final step in the cluster turn up is to delete the original redis-master pod that we created manually.  While it was useful for bootstrapping discovery in the cluster, we really don't want the lifespan of our sentinel to be tied to the lifespan of one of our redis servers, and now that we have a successful, replicated redis sentinel service up and running, the binding is unnecessary.

Delete the master as follows:

```sh
kubectl delete pods redis-master
```

Now let's take a close look at what happens after this pod is deleted.  There are three things that happen:

  1. The redis replication controller notices that its desired state is 3 replicas, but there are currently only 2 replicas, and so it creates a new redis server to bring the replica count back up to 3
  2. The redis-sentinel replication controller likewise notices the missing sentinel, and also creates a new sentinel.
  3. The redis sentinels themselves, realize that the master has disappeared from the cluster, and begin the election procedure for selecting a new master.  They perform this election and selection, and chose one of the existing redis server replicas to be the new master.


here is the summary of commands:

```
# Create a bootstrap master
kubectl create -f examples/redis/redis-master.yaml

# Create a service to track the sentinels
kubectl create -f examples/redis/redis-sentinel-service.yaml

# Create a replication controller for redis servers
kubectl create -f examples/redis/redis-controller.yaml

# Create a replication controller for redis sentinels
kubectl create -f examples/redis/redis-sentinel-controller.yaml

# Scale both replication controllers
kubectl scale rc redis --replicas=3
kubectl scale rc redis-sentinel --replicas=3

# Delete the original master pod
kubectl delete pods redis-master
```

More information please see https://github.com/kubernetes/kubernetes/tree/release-1.0/examples/redis


