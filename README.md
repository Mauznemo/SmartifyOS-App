# SmartifyOS App

>[!CAUTION]
>**Disclaimer:** This software is currently in the development phase and is intended for developers. It is not suitable for general use in vehicles yet.

## About

### Short description:
SmartifyOS is a base application (source code) that makes it easy for you to create a custom GUI for a DIY infotainment system in older cars. It is based on the [Unity Game Engine](https://unity.com/), which means you have almost unlimited possibilities to customize it to your liking.

[More](https://smartify-os.com/about)

### This repo contains:
The Android app for adding quick settings tiles that let you (via BLE):
1. Lock and unlock the car
2. Open the trunk
3. Auto lock the car when the phone is out of range and auto unlock if it is near

## How to contribute
First have a look at the **[Contribution guidelines for this project](CONTRIBUTING.md)**.

1. Go to the repository’s GitHub page and click the “Fork” button to create a copy of the repository in your own GitHub account.
2. Clone your new repo
   ```
   git clone https://github.com/your-username/SmartifyOS-App.git
   ```
1. Cd into its directory
   ```
   cd SmartifyOS-App
   ```
2. Add the Main Repository as a Remote
   ```
   git remote add upstream https://github.com/Mauznemo/SmartifyOS-App.git
   ```
2. Open the directory in [Android Studio](https://developer.android.com/studio)

### Creating a pull request

1. Navigate to Your Forked Repository
2. Compare & Pull Request:
   - GitHub usually detects recent pushes and will show a prompt asking if you want to create a pull request. If this prompt appears, click on "Compare & pull request."
   - If the prompt does not appear, click the "Pull requests" tab, then click the "New pull request" button.
3. Select the Base and Compare Branches:
   - Base repository: This should be the original repository you forked from.
   - Base branch: Typically, this is the main or master branch of the original repository.
   - Head repository: This should be your forked repository.
   - Compare branch: Select the branch you just pushed.
4. Create Pull Request and make sure to follow the [Pull Request Guidelines](CONTRIBUTING.md#pull-request-guidelines)

## Related projects
**[Main Repository - SmartifyOS](https://github.com/Mauznemo/SmartifyOS)**

**[Arduino BLE and Lock code - SmartMiata](https://github.com/Mauznemo/SmartMiata/blob/main/Arduino/miata-central-lock-controller/miata-central-lock-controller.ino)**