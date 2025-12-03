package org.aistudytool.aistudytool;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;


public class LlamaLLM {

    private final Ollama ollama;
    private static final String MODEL = "llama3.2:latest";

    public LlamaLLM() {
        this.ollama = new Ollama("http://localhost:11434");
        ollama.setRequestTimeoutSeconds(60);
    }


    public String ask(String prompt) throws OllamaException {
        OllamaChatRequest req = OllamaChatRequest.builder()
                .withModel(MODEL)
                .withMessage(OllamaChatMessageRole.USER, prompt)
                .build();

        OllamaChatResult res = ollama.chat(req, null);

        String text = extractAssistantMessage(res);

        System.out.println("FINAL EXTRACTED LLM MESSAGE:\n" + text);

        return text;
    }

    private String extractAssistantMessage(OllamaChatResult result) {
        StringBuilder sb = new StringBuilder();

        for (Object msgObj : result.getChatHistory()) {
            try {
                Class<?> cls = msgObj.getClass();

                Object roleObj = cls.getMethod("getRole").invoke(msgObj);
                String role = (roleObj == null) ? "" : roleObj.toString();

                if (!"assistant".equalsIgnoreCase(role)) {
                    continue;
                }

                Object responseObj = cls.getMethod("getResponse").invoke(msgObj);
                if (responseObj != null) sb.append(responseObj);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sb.toString().trim();
    }

    public Flashcard generateFlashcard(String sourceText) throws OllamaException {
        String prompt =
                "Turn the following study material into a single flashcard.\n\n" +
                        "Return ONLY in this format:\n" +
                        "QUESTION: <text>\n" +
                        "ANSWER: <text>\n\n" +
                        "Content:\n" + sourceText;

        String output = ask(prompt);

        String q = extract(output, "QUESTION:", "ANSWER:");
        String a = extract(output, "ANSWER:", null);

        return new Flashcard(q, a);
    }

    public String generateStudyGuide(String deckContents) throws Exception {
        String prompt = """
    You are an expert educator.
    Convert the following flashcards into a structured, easy-to-read **study guide**:

    ---- FLASHCARD CONTENT ----
    %s
    ---------------------------

    Produce a study guide with the following structure:

    # Study Guide

    ## Overview
    - 3–5 sentence summary of the key ideas

    ## Key Concepts
    - bullet list of important terms, definitions, and explanations

    ## Deep-Dive Explanations
    - 2–5 detailed sections, each covering an important idea

    ## Mnemonics & Memory Tricks
    - provide useful mnemonic devices or analogies

    ## Quick Facts / Must Know
    - short bullet points with high-value info

    ## Possible Test Questions
    - at least 5 sample questions (not the same as the flashcards)

    Keep formatting clean and readable.
    """.formatted(deckContents);

        return ask(prompt);
    }


    private String extract(String text, String startKey, String endKey) {
        try {
            int start = text.indexOf(startKey);
            if (start < 0) return "";
            start += startKey.length();

            int end = (endKey == null) ? text.length() : text.indexOf(endKey, start);
            if (end < 0) end = text.length();

            return text.substring(start, end).trim();
        }
        catch (Exception e) {
            return "";
        }
    }
}
