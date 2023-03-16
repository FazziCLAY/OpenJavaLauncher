# OpenJavaLauncher
Open-source java minecraft launcher (pro)

# Help me with the GUI
I don't know how to make beautiful graphical interfaces. If you want to help, make a fork and pullrequest

# Screenshots
![2023-03-16_17-44](https://user-images.githubusercontent.com/68351787/225690699-fac9974a-91fa-4aaf-a81a-376bdea5ace6.png)

# File structure
`java -jar OpenJavaLauncher.jar mainFolderName`
```css
mainFolderName
| openjavalauncher.json
| versions
  | version_manifest_v2.json
  | 1.19.4
    | 1.19.4.json
    | 1.19.4.jar
| libraries
  | <contains libraries>
| assets
  | objects
  | indexes
```

## openjavalauncher.json
```json
{
  "windowStartHeight": 700,
  "windowStartWidth": 500,
  "userProfiles": [
    {
      "profileUUID": "8be91f48-9a2b-4c91-bb19-f21e427dea6c",
      "name": "My first profile: Steve",
      "nickname": "Steve",
      "isDemo": false,
      "uuid": "6e65b2ac96c746bda2e766ee81500487"
    }
  ],
  "latestSave": 1678978837280,
  "language": "en_us",
  "selectedUserProfile": "8be91f48-9a2b-4c91-bb19-f21e427dea6c",
  "selectedGameProfile": "1bc0d011-06a7-4b93-a3c8-b5b4d905b42b",
  "formatVersion": 0,
  "gameProfiles": [
    {
      "jvmPath": "/bin/java",
      "versionId": "1.19.4",
      "profileUUID": "1bc0d011-06a7-4b93-a3c8-b5b4d905b42b",
      "downloadMissingAssets": true,
      "windowHeight": 0,
      "name": "Latest for FazziCRAFT",
      "gameDirectory": "/home/user/.minecraft",
      "jvmArguments": "-Xmx2048M -Xms2048M -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -Dfile.encoding=UTF-8",
      "windowWidth": 0
    }
  ]
}
```
