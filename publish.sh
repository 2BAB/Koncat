#!/usr/bin/env bash

# Keep the order as followed one may depend on previous one
MODULE_ARRAY=('koncat-contract' 'koncat-gradle-plugin' 'koncat-processor-api')
for module in "${MODULE_ARRAY[@]}"
do
./gradlew :"$module":publishKoncatArtifactPublicationToSonatypeRepository
done

./gradlew releaseArtifactsToGithub