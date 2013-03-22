Welcome to WUIC (Web UI Compressor) 
====

The library which minifies your scripts (Javascript/CSS), computes your image's sprites and supports aggregation for you.
===

The library has been designed to guarantee that good practices around web resources will be always applied when you deploy in production.

Many websites have poor performances due to the many RTTs (round trip time) performed to load images, javascript and css files.
A lot of developers know that it is recommanded to aggregate those files to reduce the number of RTTs. Moreover, some tools like
YUICompressor can reduce the size of your CSS and Javascript files to speed up the page loading.

However, people do not apply those good practices. The truth is that it is a too expensive task in time in a continuous delivery. People don't the time
to aggregate the files, minify (in case of scripts) or create sprites (in case of images) and change the import statement in there HTML
code.

The purpose of WUIC is to help the developer to automate as much as possible these tasks with the maximum of flexibility to guarantee that the device will not be deprecated in the future.


