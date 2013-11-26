Tell
====

This is a Google Glass app, which adds a new voice command to the "ok glass"
main menu: "please tell".

Never again will have you have to talk to the people around you directly! Want
a sandwich? No problem, just say: "ok glass, please tell Bob to make me a
sandwich" and Glass will say "make him a sandwich, Bob"

## Functionality

This app doesn't understand English grammar, but it will try to do some helpful
rewriting to change the viewpoint of the speaker from the user to Glass itself.

Here are some examples of what you can say:

"ok glass, please tell Patrick not to brag all the time"
=> "hey Patrick, don't brag all the time"

"ok glass, please tell John that his code is unreadable"
=> "your code is unreadable, John"

"ok glass, please tell Christine to make me a sandwich"
=> "make him a sandwich, Christine"

## Limitations

There are too many to list - this app is only intended for the humor of it
existing. However, here are some particularly notable problems:

* Tell won't understand names consisting of more than one word
  "ok glass, please tell John Smith to get off my case"
  => "John Smith to get off his case"

* Tell won't be able to change verb conjugation
  "ok glass, please tell Will that he sucks"
  => "you sucks, Will"

## Running this on Glass

You can use your IDE to compile and install this app or use
[`adb`](https://developer.android.com/tools/help/adb.html)
on the command line:

    $ adb install -r Tell.apk

To start the app, say "ok glass, please tell Bob to make me a
sandwich" from the Glass clock screen.
