Install OpenJDK 25 from adoptium.net
=======

## Run the visualizer

```powershell
javac -d bin Searching\*.java
java -cp bin AlgorithmGUI
```

## Local AI chat

The app includes an `ai chat` button that connects to Ollama on your own computer.
Ollama is the recommended local chatbot runtime for this repo because it runs on
Windows, macOS, and Linux, exposes a stable localhost API, and does not require
adding external Java libraries.

Open the app and click `ai chat`. The chat window checks whether Ollama is
installed and whether the selected model is available.

On Windows, click `set up` and then `download installer` to download and open
the official Ollama installer. After installing, click `check`; if the model is
missing, click `pull model`.

On macOS or Linux, click `set up` to open Ollama's official download page, then
return to the app and click `check`.

The default endpoint is:

```text
http://localhost:11434/api/chat
```

You can change the model in the chat window after pulling another Ollama model.
