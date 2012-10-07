AntipodalWall
=============

AntipodalWall is an standalone library designed to provide a so called "masonry"
grid layout (much like Pinterest app). This layout acommodates it's children views
in a number of configurable columns letting them grow vertically as much as they need.

Why the name
============
Well... the layout looks much like a masonry wall, but inverted. Like in the antipodes.

How to use
==========
Assuming you’re using the Eclipse Development Environment with the ADT plugin version 0.9.7
or greater you can include AntipodalWall as a library project. Create a new Android project
in Eclipse using the library/ folder as the existing source. Then, in your project properties,
add the created project under the ‘Libraries’ section of the ‘Android’ category.

Then in your XML layout file, you can add the layout like this:

    <com.antipodalwall.AntipodalWallLayout xmlns:android="http://schemas.android.com/apk/res/android"
    	xmlns:app="http://schemas.android.com/apk/res-auto"
    	android:id="@+id/antipodal_wall"
    	android:layout_width="match_parent"
    	android:layout_height="wrap_content"
	android:scrollbars="vertical"
    	app:columns="2">
        <!-- Your child views go here -->
    </com.antipodalwall.AntipodalWallLayout>

You can also add view programatically from your activity like this:

    AntipodalWallLayout layout = (AntipodalWallLayout)findViewById(R.id.antipodal_wall);
    TextView tv = new TextView(this);
    tv.setText("This TextView has been added from code");
    layout.addView(tv);

There is a sample app project that already implements the layout in the sample_app/ folder.

Developed By
============

* Daniel López Lacalle - <expilu@gmail.com>

License
=======

    Copyright 2012 Daniel López Lacalle

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
