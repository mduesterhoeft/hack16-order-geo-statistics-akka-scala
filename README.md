Geographical Order Statistics App for ePages6 with Akka
=========================

This is an Akka based epages app that is based on https://github.com/ePages-de/hack16-order-geo-statistics and tries to implement similar functionality with akka.

The app can
- periodically read [order data](https://developer.epages.com/apps/api-reference/resource-orders.html) from a configured shop
- enrich the data with geo points for the billing and shipment address
- index the data into elasticsearch
- provide dashboards to visualize order related statistics mainly in regards to geographical aspects
- user geoserver to display order related geodata on top of other data layers

### Run the app

#### Run the infrastructure

The infrastructure components:
- [elasticsearch](https://www.elastic.co/products/elasticsearch)
- [kibana](https://www.elastic.co/products/kibana)
- [geoserver](http://geoserver.org/)

can be started using docker-compose

```bash
docker-compose up -d
```

> Some of the docker images are private for the epages organization so you need at least read access for `epages/ng-elasticsearch` and `epages/ng-kibana`.