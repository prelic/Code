@echo off
for /L %%a in (1, 1, 10) do (
start startserver.bat
start startplayer1.bat
start startplayer2.bat
echo %%a
ping 123.45.67.89 -n 1 -w 7500 > nul
)

EXIT