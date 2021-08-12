[![](https://jitpack.io/v/MyselfRebel/RebelUtil.svg)](https://jitpack.io/#MyselfRebel/RebelUtil)
# RebelUtil
You can easily execute permission dependent functions using this library


## Step 1: Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
		}
   }
```


## Step 2: Add the dependency:
```
dependencies {
	  implementation 'com.github.MyselfRebel:RebelUtil:0.1.0'
}
```

## Example: How to use:
Make sure to add these permissions in the manifest too, For example, here we are trying to access EXTERNAL_STORAGE, CONTACTS, CAMERA<br>
If you set "enablePermissionString" to be true, then make sure to add "{%permissions%}" to your permission_request string
```
String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA};

boolean enablePermissionString = true;

SafeActionPerformer.performAction(context, permissions, enablePermissionString,
                getString(R.string.permission_request), this::doTask);
```

```
private void doTask() {
  // do your task for which the permissions were required..
}
```

```
<string name="permission_request">Please provide {%permissions%} permission(s) to continue</string>
```
## Permission String
![alt text](https://github.com/[MyselRebel]/[RebelUtil]/[master]/permissionString.jpg?raw=true)
