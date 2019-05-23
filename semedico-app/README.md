# semedico-app

This is a UIMA pipeline reading preprocessed CAS data from a database and feeding it to an ElasticSearch server.

# Usage

While the project contains a resource directory structure holding test examples of all required resource files (e.g. concept ID mappings), production resources are large and cumbersome for branching and versioning purposes. This is why most of the time the production resources will reside in an external directory as can automatically be created by the production resource creation script in [semedico-resource-management](https://github.com/khituras/semedico-resource-management).

To have the pipelines use a specific set of resources, just let the classpath point to the respective semedico-app base repository.
