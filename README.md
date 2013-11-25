jntlm
=====
Sometimes you are in an environment where you have to cope with an NTLM Proxy which is using an SSO mechanism based on your Windows credentials. This will work fine for your browser since most of them have implemented the authentication mechanism used for this proxy. There are some tools however that can't handle the exotic negotiation mechanism based on your authentication ticket. E.g. npm, git or dropbox. For these cases you can use **jntlm** as an intermediate proxy.
usage
-----
Download the [current zip](https://github.com/wdekker/jntlm/archive/master.zip) from this page (cause you probably can't use git yet). Unpack and run **jntlm.bat** with the following arguments: `proxyHost proxyPort`.

E.g:`C:\dev\jntlm>jntlm.bat proxy.example.com 8080`

Optional argument are:

* `listenPort` (defaults to 4242)
* `open` (literal that binds to the local network instead of the loopback interface)

**Have fun!**

copyright
---------
Copyright © 2013 Willem Dekker
This work is free. You can redistribute it and/or modify it under the
terms of the Do What The Fuck You Want To Public License, Version 2,
as published by Sam Hocevar. See the COPYING file for more details.

warranty
--------
This program is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar. See [http://www.wtfpl.net/](http://www.wtfpl.net/) for more details.