@echo off
set DIR="."
for /R %DIR% %%f in (*.*) do ( 
C:\msys64\usr\bin\sed.exe -i 's/%1/%2/g' %%f
)