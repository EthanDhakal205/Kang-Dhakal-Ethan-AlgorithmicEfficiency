import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

class AiChatDialog extends JDialog {
    private static final Font SANS = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font MONO = new Font("JetBrains Mono", Font.PLAIN, 13);

    private final Color bg;
    private final Color panel;
    private final Color card;
    private final Color border;
    private final Color accent;
    private final Color text;
    private final Color textDim;
    private final JTextArea transcriptArea = new JTextArea();
    private final JTextArea promptArea = new JTextArea(3, 48);
    private final JTextField modelField = new JTextField(OllamaChatClient.DEFAULT_MODEL, 14);
    private final JTextField endpointField = new JTextField(OllamaChatClient.DEFAULT_ENDPOINT, 28);
    private final JButton sendButton = new JButton("send");
    private final JButton setupButton = new JButton("set up");
    private final JButton checkButton = new JButton("check");
    private final JButton pullButton = new JButton("pull model");
    private final JLabel statusLabel = new JLabel("checking Ollama...");
    private final OllamaChatClient client = new OllamaChatClient();
    private final OllamaSetupService setupService = new OllamaSetupService();
    private final List<OllamaChatClient.Message> history = new ArrayList<>();
    private final String selectedAlgorithm;
    private boolean readyToChat = false;

    AiChatDialog(JFrame owner, boolean darkMode, String selectedAlgorithm) {
        super(owner, "Local AI Chat", false);
        this.selectedAlgorithm = selectedAlgorithm;
        bg = darkMode ? new Color(8, 13, 26) : new Color(248, 250, 252);
        panel = darkMode ? new Color(13, 20, 35) : Color.WHITE;
        card = darkMode ? new Color(20, 30, 48) : Color.WHITE;
        border = darkMode ? new Color(42, 54, 74) : new Color(211, 219, 232);
        accent = darkMode ? new Color(96, 165, 250) : new Color(37, 99, 235);
        text = darkMode ? new Color(241, 245, 249) : new Color(15, 23, 42);
        textDim = darkMode ? new Color(180, 190, 205) : new Color(71, 85, 105);
        build();
    }

    private void build() {
        getContentPane().setBackground(bg);
        setLayout(new BorderLayout(0, 12));
        setPreferredSize(new Dimension(780, 620));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTranscript(), BorderLayout.CENTER);
        add(buildComposer(), BorderLayout.SOUTH);

        appendAssistant("I will check the local Ollama setup. Use Set up or Pull model if anything is missing.");
        checkLocalAiStatus();

        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setBackground(panel);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Local AI Chat");
        title.setFont(TITLE);
        title.setForeground(text);
        JLabel subtitle = new JLabel("Ollama runs the model on this computer. Current topic: " + selectedAlgorithm);
        subtitle.setFont(SANS);
        subtitle.setForeground(textDim);
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(3));
        titleStack.add(subtitle);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        fields.setOpaque(false);
        fields.add(label("model"));
        fields.add(styleField(modelField));
        fields.add(label("endpoint"));
        fields.add(styleField(endpointField));

        JPanel setupControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        setupControls.setOpaque(false);
        setupControls.add(styleButton(setupButton, false));
        setupControls.add(styleButton(pullButton, true));
        setupControls.add(styleButton(checkButton, false));

        setupButton.addActionListener(e -> openSetupDialog());
        checkButton.addActionListener(e -> checkLocalAiStatus());
        pullButton.addActionListener(e -> pullSelectedModel());
        pullButton.setEnabled(false);

        JPanel rightStack = new JPanel();
        rightStack.setOpaque(false);
        rightStack.setLayout(new BoxLayout(rightStack, BoxLayout.Y_AXIS));
        rightStack.add(fields);
        rightStack.add(Box.createVerticalStrut(8));
        rightStack.add(setupControls);

        statusLabel.setFont(SANS);
        statusLabel.setForeground(textDim);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        header.add(titleStack, BorderLayout.WEST);
        header.add(rightStack, BorderLayout.EAST);
        header.add(statusLabel, BorderLayout.SOUTH);
        return header;
    }

    private JScrollPane buildTranscript() {
        transcriptArea.setEditable(false);
        transcriptArea.setLineWrap(true);
        transcriptArea.setWrapStyleWord(true);
        transcriptArea.setFont(SANS);
        transcriptArea.setBackground(bg);
        transcriptArea.setForeground(text);
        transcriptArea.setBorder(new EmptyBorder(18, 22, 18, 22));

        JScrollPane scroll = new JScrollPane(transcriptArea);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, border));
        scroll.getViewport().setBackground(bg);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel buildComposer() {
        JPanel composer = new JPanel(new BorderLayout(10, 10));
        composer.setBackground(panel);
        composer.setBorder(new EmptyBorder(14, 20, 16, 20));

        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(SANS);
        promptArea.setBackground(card);
        promptArea.setForeground(text);
        promptArea.setCaretColor(accent);
        promptArea.setBorder(new EmptyBorder(9, 11, 9, 11));
        promptArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER && event.isControlDown()) {
                    event.consume();
                    sendPrompt();
                }
            }
        });

        sendButton.setFont(SANS_B);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(accent);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(88, 42));
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendPrompt());

        composer.add(new JScrollPane(promptArea), BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);
        return composer;
    }

    private JButton styleButton(JButton button, boolean primary) {
        button.setFont(SANS_B);
        button.setForeground(primary ? Color.WHITE : textDim);
        button.setBackground(primary ? accent : card);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primary ? accent : border, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private JLabel label(String value) {
        JLabel label = new JLabel(value);
        label.setFont(SANS);
        label.setForeground(textDim);
        return label;
    }

    private JTextField styleField(JTextField field) {
        field.setFont(MONO);
        field.setBackground(card);
        field.setForeground(text);
        field.setCaretColor(accent);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private void sendPrompt() {
        String prompt = promptArea.getText().trim();
        if (prompt.isEmpty() || !sendButton.isEnabled()) {
            return;
        }

        promptArea.setText("");
        appendUser(prompt);
        history.add(new OllamaChatClient.Message("user", prompt));
        trimHistory();
        setSending(true);

        List<OllamaChatClient.Message> requestMessages = new ArrayList<>();
        requestMessages.add(new OllamaChatClient.Message("system",
            "You are a concise algorithm tutor embedded in a Java visualizer. "
                + "Explain search and sorting concepts clearly, prefer practical examples, "
                + "and relate answers to " + selectedAlgorithm + " when useful."));
        requestMessages.addAll(history);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return client.chat(endpointField.getText(), modelField.getText(), requestMessages);
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    history.add(new OllamaChatClient.Message("assistant", reply));
                    trimHistory();
                    appendAssistant(reply);
                } catch (Exception ex) {
                    appendAssistant("I could not reach Ollama. Check that Ollama is installed, running, "
                        + "and has the model pulled with `ollama pull " + modelField.getText().trim() + "`.\n\n"
                        + rootMessage(ex));
                } finally {
                    setSending(false);
                }
            }
        };
        worker.execute();
    }

    private void checkLocalAiStatus() {
        setControlsBusy("checking Ollama...");
        SwingWorker<LocalAiStatus, Void> worker = new SwingWorker<>() {
            @Override
            protected LocalAiStatus doInBackground() throws Exception {
                boolean installed = setupService.isOllamaInstalled();
                boolean serverAvailable = client.isServerAvailable(endpointField.getText());
                if (!serverAvailable && installed) {
                    try {
                        setupService.startOllamaIfPossible();
                    } catch (IOException ignored) {
                        // Recheck below gives a clearer status to the user.
                    }
                    serverAvailable = waitForServer();
                }
                boolean modelAvailable = false;
                String detail = "";
                if (serverAvailable) {
                    try {
                        modelAvailable = client.hasModel(endpointField.getText(), currentModel());
                    } catch (IOException ex) {
                        detail = ex.getMessage();
                    }
                }
                return new LocalAiStatus(installed, serverAvailable, modelAvailable, detail);
            }

            @Override
            protected void done() {
                try {
                    applyStatus(get());
                } catch (Exception ex) {
                    setStatus("Could not check Ollama: " + rootMessage(ex), false, false);
                }
            }
        };
        worker.execute();
    }

    private boolean waitForServer() throws InterruptedException {
        for (int i = 0; i < 8; i++) {
            if (client.isServerAvailable(endpointField.getText())) {
                return true;
            }
            Thread.sleep(750);
        }
        return false;
    }

    private void applyStatus(LocalAiStatus status) {
        if (status.serverAvailable && status.modelAvailable) {
            setStatus("Ollama is ready with " + currentModel() + ".", true, false);
            return;
        }
        if (status.serverAvailable) {
            if (status.detail != null && !status.detail.isBlank()) {
                setStatus("Ollama is running, but model check failed: " + status.detail, false, false);
                return;
            }
            setStatus("Ollama is running. Pull " + currentModel() + " to enable chat.", false, true);
            return;
        }
        if (status.installed) {
            setStatus("Ollama is installed, but the local server is not responding. Click Check to retry.", false, false);
            return;
        }
        setStatus("Ollama is not installed. Click Set up to download it.", false, false);
    }

    private void pullSelectedModel() {
        String model = currentModel();
        setControlsBusy("pulling " + model + "...");
        appendAssistant("Pulling `" + model + "` through Ollama. This can take a while the first time.");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.pullModel(endpointField.getText(), model,
                    progress -> SwingUtilities.invokeLater(() -> statusLabel.setText(progress)));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    appendAssistant("`" + model + "` is ready.");
                    checkLocalAiStatus();
                } catch (Exception ex) {
                    setStatus("Could not pull model: " + rootMessage(ex), false, true);
                    appendAssistant("The model download failed.\n\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void openSetupDialog() {
        JDialog dialog = new JDialog(this, "Set Up Ollama", false);
        dialog.getContentPane().setBackground(bg);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setPreferredSize(new Dimension(560, 280));

        JLabel title = new JLabel("Set up local AI");
        title.setFont(TITLE);
        title.setForeground(text);

        JTextArea body = new JTextArea(setupService.platformInstructions());
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(SANS);
        body.setBackground(bg);
        body.setForeground(textDim);
        body.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel setupStatus = new JLabel("Ready.");
        setupStatus.setFont(SANS);
        setupStatus.setForeground(textDim);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(bg);
        content.setBorder(new EmptyBorder(18, 20, 8, 20));
        content.add(title, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.add(setupStatus, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(panel);
        actions.setBorder(new EmptyBorder(12, 16, 12, 16));

        JButton downloadButton = styleButton(new JButton(setupService.isWindows()
            ? "download installer"
            : "open download page"), true);
        JButton recheckButton = styleButton(new JButton("check again"), false);
        JButton closeButton = styleButton(new JButton("close"), false);

        downloadButton.addActionListener(e -> {
            if (setupService.isWindows()) {
                downloadWindowsInstaller(downloadButton, setupStatus);
            } else {
                try {
                    setupService.openDownloadPage();
                    setupStatus.setText("Opened the official Ollama download page.");
                } catch (IOException ex) {
                    setupStatus.setText(rootMessage(ex));
                    JOptionPane.showMessageDialog(dialog, OllamaSetupService.DOWNLOAD_PAGE,
                        "Open this URL", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        recheckButton.addActionListener(e -> checkLocalAiStatus());
        closeButton.addActionListener(e -> dialog.dispose());

        actions.add(downloadButton);
        actions.add(recheckButton);
        actions.add(closeButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void downloadWindowsInstaller(JButton downloadButton, JLabel setupStatus) {
        downloadButton.setEnabled(false);
        setupStatus.setText("Starting download...");

        SwingWorker<Path, Void> worker = new SwingWorker<>() {
            @Override
            protected Path doInBackground() throws Exception {
                return setupService.downloadWindowsInstaller(
                    progress -> SwingUtilities.invokeLater(() -> setupStatus.setText(progress)));
            }

            @Override
            protected void done() {
                downloadButton.setEnabled(true);
                try {
                    Path installer = get();
                    setupStatus.setText("Opening installer...");
                    setupService.launchInstaller(installer);
                    appendAssistant("The Ollama installer is open. Finish it, then click Check in the chat window.");
                } catch (Exception ex) {
                    setupStatus.setText("Download failed: " + rootMessage(ex));
                    appendAssistant("Ollama download failed.\n\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void setControlsBusy(String status) {
        readyToChat = false;
        sendButton.setEnabled(false);
        setupButton.setEnabled(false);
        checkButton.setEnabled(false);
        pullButton.setEnabled(false);
        statusLabel.setText(status);
    }

    private void setStatus(String status, boolean readyToChat, boolean canPullModel) {
        this.readyToChat = readyToChat;
        statusLabel.setText(status);
        sendButton.setEnabled(readyToChat);
        setupButton.setEnabled(true);
        checkButton.setEnabled(true);
        pullButton.setEnabled(canPullModel);
    }

    private void setSending(boolean sending) {
        sendButton.setEnabled(!sending && readyToChat);
        sendButton.setText(sending ? "..." : "send");
    }

    private String currentModel() {
        String model = modelField.getText().trim();
        return model.isEmpty() ? OllamaChatClient.DEFAULT_MODEL : model;
    }

    private void appendUser(String text) {
        append("You", text);
    }

    private void appendAssistant(String text) {
        append("AI", text);
    }

    private void append(String speaker, String text) {
        if (transcriptArea.getText().isEmpty()) {
            transcriptArea.append(speaker + ":\n" + text + "\n");
        } else {
            transcriptArea.append("\n" + speaker + ":\n" + text + "\n");
        }
        transcriptArea.setCaretPosition(transcriptArea.getDocument().getLength());
    }

    private void trimHistory() {
        while (history.size() > 16) {
            history.remove(0);
        }
    }

    private String rootMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private static class LocalAiStatus {
        final boolean installed;
        final boolean serverAvailable;
        final boolean modelAvailable;
        final String detail;

        LocalAiStatus(boolean installed, boolean serverAvailable, boolean modelAvailable, String detail) {
            this.installed = installed;
            this.serverAvailable = serverAvailable;
            this.modelAvailable = modelAvailable;
            this.detail = detail;
        }
    }
}
