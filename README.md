# CatchKennyRepo

Definition of The Game:

We have an image on the screen and the image changes its place on the screen at certain intervals. The goal is to touch the image and you get points by touching the images. 
Game has 10 levels and each level's duration is 15 seconds. 
In every level image gets faster and depended to that the point you get is increasing in each level.
Points of each level is equal to that level. For example in first level you get 1 point on each click, in third level you get three points in each click and this goes on.
We keep the score and max score via SharedPreferences structure.
We restart the game via intent structure.
We use AlertDialog structure at the end of the levels to print message to user.
