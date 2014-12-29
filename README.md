AKKA modelling Ping pong game.

Given two players.
First one sends "Ping" every N millis.
Second one answers "Pong" on every M pings sent.
Recieving actor often fails with an error.
The error is imitated every B millis > N and gets restored with reached amount of pings.
After K recieved pongs players switched.


N – delay between pings

B – delay between breaks

M – Pings count to pong

K – count of Pong to switch roles

G – Count of sets for game finish
