
# react-native-v5

## Getting started

`$ npm install react-native-v5 --save`

### Mostly automatic installation

`$ react-native link react-native-v5`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.V5Package;` to the imports at the top of the file
  - Add `new V5Package()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-v5'
  	project(':react-native-v5').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-v5/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-v5')
  	```


## Usage
```javascript
import V5Module from 'react-native-v5';

// TODO: What to do with the module?
V5Module;
```
  