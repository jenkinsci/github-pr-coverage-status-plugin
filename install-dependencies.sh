#!/usr/bin/env bash

# resolve all dependencies and skip logs (as it huge and Travis has limit 4Mb)
mvn dependency:resolve > /dev/null

# print resolved versions
mvn dependency:resolve