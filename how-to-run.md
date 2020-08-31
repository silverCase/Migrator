Just run the [jar file](https://github.com/Guseyn/Migrator/blob/master/migrator-1.0.0-jar-with-dependencies.jar)

```
java -jar target/migrator-1.0.0-jar-with-dependencies.jar  https://github.com/brianm/jdbi brianm-output.json
```

First argument is the url to the git repo you want to retrieve commits from. Second one is name of output json file with migration rules(you need to look at `migrationId` property, not just `id`).
