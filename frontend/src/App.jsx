import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { API_BASE_URL, apiRequest } from "./api";

const languages = [
  { code: "en", label: "English" },
  { code: "es", label: "Spanish" },
  { code: "fr", label: "French" },
  { code: "de", label: "German" },
  { code: "hi", label: "Hindi" },
  { code: "ta", label: "Tamil" },
  { code: "ar", label: "Arabic" },
  { code: "ja", label: "Japanese" }
];

const emptySessionForm = {
  title: "",
  sourceLanguage: "en",
  targetLanguage: "es"
};

const emptyMessageForm = {
  senderName: "",
  text: "",
  direction: "forward"
};

const speechLocales = {
  en: "en-US",
  es: "es-ES",
  fr: "fr-FR",
  de: "de-DE",
  hi: "hi-IN",
  ta: "ta-IN",
  ar: "ar-SA",
  ja: "ja-JP"
};

function formatLanguage(code) {
  return languages.find((language) => language.code === code)?.label || code;
}

function formatTimestamp(value) {
  return value ? new Date(value).toLocaleString() : "Just now";
}

function getSpeechRecognition() {
  if (typeof window === "undefined") {
    return null;
  }

  return window.SpeechRecognition || window.webkitSpeechRecognition || null;
}

function getSpeechSynthesis() {
  if (typeof window === "undefined") {
    return null;
  }

  return window.speechSynthesis || null;
}

function getAudioRecordingSupport() {
  if (typeof window === "undefined" || typeof navigator === "undefined") {
    return false;
  }

  return Boolean(window.MediaRecorder && navigator.mediaDevices?.getUserMedia);
}

function getSpeakableText(text) {
  return (text || "").replace(/^\[[A-Z-]+->[A-Z-]+\]\s*/, "").trim();
}

function getPreferredRecordingMimeType() {
  if (typeof window === "undefined" || !window.MediaRecorder?.isTypeSupported) {
    return "";
  }

  const candidates = [
    "audio/webm;codecs=opus",
    "audio/webm",
    "audio/ogg;codecs=opus",
    "audio/ogg"
  ];

  return candidates.find((mimeType) => window.MediaRecorder.isTypeSupported(mimeType)) || "";
}

function getDirectionDetails(session, direction) {
  if (!session) {
    return {
      sourceLanguage: "",
      targetLanguage: "",
      sourceLabel: "",
      targetLabel: ""
    };
  }

  const isReverse = direction === "reverse";
  const sourceLanguage = isReverse ? session.targetLanguage : session.sourceLanguage;
  const targetLanguage = isReverse ? session.sourceLanguage : session.targetLanguage;

  return {
    sourceLanguage,
    targetLanguage,
    sourceLabel: formatLanguage(sourceLanguage),
    targetLabel: formatLanguage(targetLanguage)
  };
}

function AuthPanel({
  authMode,
  authForm,
  authMessage,
  loading,
  onModeChange,
  onChange,
  onSubmit
}) {
  return (
    <section className="panel auth-panel">
      <div className="eyebrow">Welcome to TalkLingo</div>
      <h1>Translate and talk easily.</h1>
      <p className="panel-copy">
        Create a room, translate messages, and play the output.
      </p>

      <div className="toggle-row">
        <button
          type="button"
          className={authMode === "login" ? "toggle active" : "toggle"}
          onClick={() => onModeChange("login")}
        >
          Login
        </button>
        <button
          type="button"
          className={authMode === "register" ? "toggle active" : "toggle"}
          onClick={() => onModeChange("register")}
        >
          Register
        </button>
      </div>

      <form className="stack" onSubmit={onSubmit}>
        {authMode === "register" ? (
          <label>
            <span>Name</span>
            <input
              name="name"
              value={authForm.name}
              onChange={onChange}
              placeholder="Asha Raman"
              required
            />
          </label>
        ) : null}

        <label>
          <span>Email</span>
          <input
            type="email"
            name="email"
            value={authForm.email}
            onChange={onChange}
            placeholder="asha@example.com"
            required
          />
        </label>

        <label>
          <span>Password</span>
          <input
            type="password"
            name="password"
            value={authForm.password}
            onChange={onChange}
            placeholder="Enter password"
            required
          />
        </label>

        <button className="primary-button" type="submit" disabled={loading}>
          {loading ? "Please wait..." : authMode === "login" ? "Enter workspace" : "Create account"}
        </button>
      </form>

      {authMessage ? <p className="status-message">{authMessage}</p> : null}
    </section>
  );
}

function SessionList({ sessions, activeSessionId, onSelect }) {
  return (
    <section className="panel session-list-panel">
      <div className="panel-header">
        <div>
          <div className="eyebrow">Live rooms</div>
          <h2>Conversation sessions</h2>
        </div>
      </div>

      <div className="session-list">
        {sessions.length === 0 ? (
          <div className="empty-state">Create your first session to start translating messages.</div>
        ) : null}

        {sessions.map((session) => (
          <button
            type="button"
            key={session.id}
            className={session.id === activeSessionId ? "session-card active" : "session-card"}
            onClick={() => onSelect(session.id)}
          >
            <strong>{session.title}</strong>
            <span>
              {formatLanguage(session.sourceLanguage)} to {formatLanguage(session.targetLanguage)}
            </span>
            <small>{formatTimestamp(session.createdAt)}</small>
          </button>
        ))}
      </div>
    </section>
  );
}

function SessionComposer({ sessionForm, onChange, onSubmit, loading }) {
  return (
    <section className="panel composer-panel">
      <div className="panel-header">
        <div>
          <div className="eyebrow">Create session</div>
          <h2>Start a new translation room</h2>
        </div>
      </div>

      <form className="stack" onSubmit={onSubmit}>
        <label>
          <span>Session title</span>
          <input
            name="title"
            value={sessionForm.title}
            onChange={onChange}
            placeholder="Global support handoff"
            required
          />
        </label>

        <div className="field-grid">
          <label>
            <span>Source language</span>
            <select name="sourceLanguage" value={sessionForm.sourceLanguage} onChange={onChange}>
              {languages.map((language) => (
                <option key={language.code} value={language.code}>
                  {language.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>Target language</span>
            <select name="targetLanguage" value={sessionForm.targetLanguage} onChange={onChange}>
              {languages.map((language) => (
                <option key={language.code} value={language.code}>
                  {language.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <button className="secondary-button" type="submit" disabled={loading}>
          {loading ? "Creating..." : "Create session"}
        </button>
      </form>
    </section>
  );
}

function TranscriptPanel({
  activeSession,
  messageForm,
  sendingMessage,
  recordingSupported,
  isRecording,
  speechStatus,
  speechPlaybackSupported,
  onPlayAudio,
  onToggleRecording,
  onDirectionChange,
  onChange,
  onSubmit
}) {
  if (!activeSession) {
    return (
      <section className="panel transcript-panel">
        <div className="empty-state large">
          Select a session to view the transcript and send translated messages.
        </div>
      </section>
    );
  }

  const directionDetails = getDirectionDetails(activeSession, messageForm.direction);

  return (
    <section className="panel transcript-panel">
      <div className="panel-header">
        <div>
          <div className="eyebrow">Active conversation</div>
          <h2>{activeSession.title}</h2>
        </div>
        <div className="session-meta">
          <span>{formatLanguage(activeSession.sourceLanguage)}</span>
          <span>to</span>
          <span>{formatLanguage(activeSession.targetLanguage)}</span>
        </div>
      </div>

      <div className="message-stream">
        {activeSession.messages?.length ? null : (
          <div className="empty-state">No translated messages yet. Send the first one below.</div>
        )}

        {(activeSession.messages || []).map((message) => (
          <article className="message-card" key={message.id}>
            <div className="message-topline">
              <strong>{message.senderName}</strong>
              <span>{formatTimestamp(message.createdAt)}</span>
            </div>
            <p className="message-original">{message.originalText}</p>
            <p className="message-translation">{message.translatedText}</p>
            <div className="message-footer">
              <span>
                {formatLanguage(message.sourceLanguage)} to {formatLanguage(message.targetLanguage)}
              </span>
              <button
                type="button"
                className="text-button"
                onClick={() => onPlayAudio(message)}
                disabled={!speechPlaybackSupported}
              >
                {speechPlaybackSupported ? "Play voice output" : "Voice output unavailable"}
              </button>
            </div>
          </article>
        ))}
      </div>

      <form className="stack compact" onSubmit={onSubmit}>
        <div className="direction-switch" role="group" aria-label="Conversation direction">
          <button
            type="button"
            className={messageForm.direction === "forward" ? "toggle active" : "toggle"}
            onClick={() => onDirectionChange("forward")}
          >
            {formatLanguage(activeSession.sourceLanguage)} speaker
          </button>
          <button
            type="button"
            className={messageForm.direction === "reverse" ? "toggle active" : "toggle"}
            onClick={() => onDirectionChange("reverse")}
          >
            {formatLanguage(activeSession.targetLanguage)} speaker
          </button>
        </div>

        <div className="field-grid">
          <label>
            <span>Sender name</span>
            <input
              name="senderName"
              value={messageForm.senderName}
              onChange={onChange}
              placeholder="Asha"
              required
            />
          </label>

          <label>
            <span>Target output</span>
            <input
              value={`${directionDetails.sourceLabel} to ${directionDetails.targetLabel}`}
              disabled
              readOnly
            />
          </label>
        </div>

        <label>
          <span>Message or voice transcript</span>
          <textarea
            name="text"
            value={messageForm.text}
            onChange={onChange}
            placeholder={`Speak or type in ${directionDetails.sourceLabel}. The app will send translation in ${directionDetails.targetLabel}.`}
            rows="4"
            required
          />
        </label>

        <div className="composer-actions">
          <button
            type="button"
            className={isRecording ? "secondary-button voice-button listening" : "secondary-button voice-button"}
            onClick={onToggleRecording}
            disabled={!recordingSupported || sendingMessage}
          >
            {!recordingSupported ? "Voice input unavailable" : isRecording ? "Stop and translate voice" : "Start voice recording"}
          </button>

          <button className="primary-button" type="submit" disabled={sendingMessage}>
            {sendingMessage ? "Translating..." : "Translate and broadcast"}
          </button>
        </div>

        {speechStatus ? <div className="voice-status">{speechStatus}</div> : null}
      </form>
    </section>
  );
}

export default function App() {
  const [authMode, setAuthMode] = useState("login");
  const [authForm, setAuthForm] = useState({ name: "", email: "", password: "" });
  const [authMessage, setAuthMessage] = useState("");
  const [authLoading, setAuthLoading] = useState(false);
  const [token, setToken] = useState(() => localStorage.getItem("talklingo-token") || "");
  const [sessions, setSessions] = useState([]);
  const [activeSessionId, setActiveSessionId] = useState(null);
  const [activeSession, setActiveSession] = useState(null);
  const [sessionForm, setSessionForm] = useState(emptySessionForm);
  const [messageForm, setMessageForm] = useState(emptyMessageForm);
  const [creatingSession, setCreatingSession] = useState(false);
  const [sendingMessage, setSendingMessage] = useState(false);
  const [workspaceMessage, setWorkspaceMessage] = useState("");
  const [isRecording, setIsRecording] = useState(false);
  const [speechStatus, setSpeechStatus] = useState("");
  const stompClientRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const mediaStreamRef = useRef(null);
  const audioChunksRef = useRef([]);
  const spokenMessageIdsRef = useRef(new Set());
  const isAuthenticated = Boolean(token);
  const recordingSupported = getAudioRecordingSupport();
  const speechPlaybackSupported = Boolean(getSpeechSynthesis());

  useEffect(() => {
    if (!isAuthenticated) {
      return undefined;
    }

    loadSessions();

    const intervalId = window.setInterval(() => {
      loadSessions(false);
      if (activeSessionId) {
        loadSessionDetails(activeSessionId, false);
      }
    }, 8000);

    return () => window.clearInterval(intervalId);
  }, [isAuthenticated, activeSessionId]);

  useEffect(() => {
    if (!activeSessionId) {
      setActiveSession(null);
      stopRecording();
      return undefined;
    }

    loadSessionDetails(activeSessionId);

    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/sessions/${activeSessionId}`, (frame) => {
          const incomingMessage = JSON.parse(frame.body);
          setActiveSession((current) => {
            if (!current || current.id !== activeSessionId) {
              return current;
            }

            const messages = current.messages || [];
            const alreadyExists = messages.some((message) => message.id === incomingMessage.id);
            return {
              ...current,
              messages: alreadyExists ? messages : [...messages, incomingMessage]
            };
          });
        });
      }
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [activeSessionId]);

  useEffect(() => () => {
    stopRecording();
    stopSpeechPlayback();
  }, []);

  useEffect(() => {
    const latestMessage = activeSession?.messages?.[activeSession.messages.length - 1];
    if (!latestMessage || !speechPlaybackSupported) {
      return;
    }

    if (spokenMessageIdsRef.current.has(latestMessage.id)) {
      return;
    }

    spokenMessageIdsRef.current.add(latestMessage.id);
    playTranslatedAudio(latestMessage, false);
  }, [activeSession, speechPlaybackSupported]);

  async function loadSessions(showErrors = true) {
    try {
      const result = await apiRequest("/api/sessions");
      setSessions(result);

      if (!activeSessionId && result.length > 0) {
        setActiveSessionId(result[0].id);
      }
    } catch (error) {
      if (showErrors) {
        setWorkspaceMessage(error.message);
      }
    }
  }

  async function loadSessionDetails(sessionId, showErrors = true) {
    try {
      const result = await apiRequest(`/api/sessions/${sessionId}`);
      setActiveSession(result);
    } catch (error) {
      if (showErrors) {
        setWorkspaceMessage(error.message);
      }
    }
  }

  async function handleAuthSubmit(event) {
    event.preventDefault();
    setAuthLoading(true);
    setAuthMessage("");

    try {
      if (authMode === "register") {
        await apiRequest("/api/users/register", {
          method: "POST",
          body: JSON.stringify({
            name: authForm.name,
            email: authForm.email,
            password: authForm.password
          })
        });
        setAuthMessage("Registration complete. You can log in now.");
        setAuthMode("login");
      } else {
        const receivedToken = await apiRequest("/api/users/login", {
          method: "POST",
          body: JSON.stringify({
            email: authForm.email,
            password: authForm.password
          })
        });
        localStorage.setItem("talklingo-token", receivedToken.token);
        setToken(receivedToken.token);
        setWorkspaceMessage("You are connected to the TalkLingo workspace.");
      }
    } catch (error) {
      setAuthMessage(error.message);
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleSessionSubmit(event) {
    event.preventDefault();
    setCreatingSession(true);
    setWorkspaceMessage("");

    try {
      const createdSession = await apiRequest("/api/sessions", {
        method: "POST",
        body: JSON.stringify(sessionForm)
      });
      setSessionForm(emptySessionForm);
      await loadSessions(false);
      setActiveSessionId(createdSession.id);
      setWorkspaceMessage("Session created successfully.");
    } catch (error) {
      setWorkspaceMessage(error.message);
    } finally {
      setCreatingSession(false);
    }
  }

  async function handleMessageSubmit(event) {
    event.preventDefault();
    if (!activeSession) {
      return;
    }

    setSendingMessage(true);
    setWorkspaceMessage("");

    try {
      const directionDetails = getDirectionDetails(activeSession, messageForm.direction);
      const newMessage = await apiRequest(`/api/sessions/${activeSession.id}/messages`, {
        method: "POST",
        body: JSON.stringify({
          senderName: messageForm.senderName,
          sourceLanguage: directionDetails.sourceLanguage,
          targetLanguage: directionDetails.targetLanguage,
          text: messageForm.text
        })
      });

      setActiveSession((current) => ({
        ...current,
        messages: [...(current?.messages || []), newMessage]
      }));
      setMessageForm((current) => ({ ...current, text: "" }));
      setWorkspaceMessage("Translation delivered to the active session.");
    } catch (error) {
      setWorkspaceMessage(error.message);
    } finally {
      setSendingMessage(false);
    }
  }

  function handleAuthFieldChange(event) {
    const { name, value } = event.target;
    setAuthForm((current) => ({ ...current, [name]: value }));
  }

  function handleSessionFieldChange(event) {
    const { name, value } = event.target;
    setSessionForm((current) => ({ ...current, [name]: value }));
  }

  function handleMessageFieldChange(event) {
    const { name, value } = event.target;
    setMessageForm((current) => ({ ...current, [name]: value }));
  }

  function handleDirectionChange(direction) {
    stopRecording();
    stopSpeechPlayback();
    setSpeechStatus("");
    setMessageForm((current) => ({
      ...current,
      direction,
      text: ""
    }));
  }

  function stopRecording() {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== "inactive") {
      mediaRecorderRef.current.stop();
    }

    if (mediaStreamRef.current) {
      mediaStreamRef.current.getTracks().forEach((track) => track.stop());
      mediaStreamRef.current = null;
    }

    mediaRecorderRef.current = null;
    audioChunksRef.current = [];
    setIsRecording(false);
  }

  function stopSpeechPlayback() {
    const synthesis = getSpeechSynthesis();
    if (synthesis) {
      synthesis.cancel();
    }
  }

  function playTranslatedAudio(message, showStatus = true) {
    const synthesis = getSpeechSynthesis();
    if (!synthesis) {
      if (showStatus) {
        setSpeechStatus("Voice playback is not supported in this browser.");
      }
      return;
    }

    const textToSpeak = getSpeakableText(message.translatedText) || getSpeakableText(message.originalText);
    if (!textToSpeak) {
      if (showStatus) {
        setSpeechStatus("There is no translated text available to speak.");
      }
      return;
    }

    synthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(textToSpeak);
    utterance.lang = speechLocales[message.targetLanguage] || message.targetLanguage || "en-US";
    utterance.rate = 0.95;
    utterance.pitch = 1;

    if (showStatus) {
      setSpeechStatus(`Playing voice output in ${formatLanguage(message.targetLanguage)}.`);
    }

    utterance.onend = () => {
      if (showStatus) {
        setSpeechStatus("Voice playback finished.");
      }
    };

    utterance.onerror = () => {
      if (showStatus) {
        setSpeechStatus("Voice playback failed in this browser.");
      }
    };

    synthesis.speak(utterance);
  }

  async function handleToggleRecording() {
    if (!activeSession || sendingMessage) {
      return;
    }

    if (!recordingSupported) {
      setSpeechStatus("This browser does not support voice recording. You can still type your message.");
      return;
    }

    if (isRecording) {
      setSpeechStatus("Finishing voice recording...");
      mediaRecorderRef.current?.stop();
      return;
    }

    const directionDetails = getDirectionDetails(activeSession, messageForm.direction);

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mimeType = getPreferredRecordingMimeType();
      const recorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);

      mediaStreamRef.current = stream;
      mediaRecorderRef.current = recorder;
      audioChunksRef.current = [];

      recorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      recorder.onstart = () => {
        setIsRecording(true);
        setSpeechStatus(`Recording in ${directionDetails.sourceLabel}...`);
      };

      recorder.onerror = () => {
        setSpeechStatus("Voice recording failed. Please try again.");
        stopRecording();
      };

      recorder.onstop = async () => {
        const finalMimeType = recorder.mimeType || mimeType || "audio/webm";
        const audioBlob = new Blob(audioChunksRef.current, { type: finalMimeType });

        if (mediaStreamRef.current) {
          mediaStreamRef.current.getTracks().forEach((track) => track.stop());
          mediaStreamRef.current = null;
        }

        mediaRecorderRef.current = null;
        audioChunksRef.current = [];
        setIsRecording(false);

        if (!audioBlob.size) {
          setSpeechStatus("No audio was recorded. Please try again.");
          return;
        }

        await submitVoiceRecording(audioBlob, finalMimeType, directionDetails);
      };

      recorder.start();
    } catch (error) {
      setSpeechStatus("Microphone access was denied or unavailable.");
    }
  }

  async function submitVoiceRecording(audioBlob, mimeType, directionDetails) {
    if (!activeSession) {
      return;
    }

    setSendingMessage(true);
    setWorkspaceMessage("");
    setSpeechStatus(`Uploading ${directionDetails.sourceLabel} audio for transcription...`);

    try {
      const extension = mimeType.includes("ogg") ? "ogg" : "webm";
      const audioFile = new File([audioBlob], `voice-message.${extension}`, { type: mimeType });
      const formData = new FormData();
      formData.append("senderName", messageForm.senderName);
      formData.append("sourceLanguage", directionDetails.sourceLanguage);
      formData.append("targetLanguage", directionDetails.targetLanguage);
      formData.append("audio", audioFile);

      const newMessage = await apiRequest(`/api/sessions/${activeSession.id}/voice-messages`, {
        method: "POST",
        body: formData
      });

      setActiveSession((current) => ({
        ...current,
        messages: [...(current?.messages || []), newMessage]
      }));
      setMessageForm((current) => ({
        ...current,
        text: ""
      }));
      setSpeechStatus(`Voice translated from ${directionDetails.sourceLabel} to ${directionDetails.targetLabel}.`);
      setWorkspaceMessage("Voice translation delivered to the active session.");
    } catch (error) {
      setSpeechStatus(error.message);
      setWorkspaceMessage(error.message);
    } finally {
      setSendingMessage(false);
    }
  }

  function handleLogout() {
    stopRecording();
    stopSpeechPlayback();
    localStorage.removeItem("talklingo-token");
    setToken("");
    setSessions([]);
    setActiveSessionId(null);
    setActiveSession(null);
    setWorkspaceMessage("You have been signed out.");
  }

  if (!isAuthenticated) {
    return (
      <main className="app-shell auth-shell">
        <div className="aurora aurora-left" />
        <div className="aurora aurora-right" />
        <AuthPanel
          authMode={authMode}
          authForm={authForm}
          authMessage={authMessage}
          loading={authLoading}
          onModeChange={setAuthMode}
          onChange={handleAuthFieldChange}
          onSubmit={handleAuthSubmit}
        />
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <div>
          <div className="eyebrow">TalkLingo workspace</div>
          <h1>Translate conversations in one room.</h1>
          <p>
            Create a room, translate messages, and play translated voice output.
          </p>
        </div>
        <button type="button" className="ghost-button" onClick={handleLogout}>
          Logout
        </button>
      </header>

      {workspaceMessage ? <div className="workspace-banner">{workspaceMessage}</div> : null}

      <section className="dashboard-grid">
        <SessionList
          sessions={sessions}
          activeSessionId={activeSessionId}
          onSelect={setActiveSessionId}
        />
        <SessionComposer
          sessionForm={sessionForm}
          onChange={handleSessionFieldChange}
          onSubmit={handleSessionSubmit}
          loading={creatingSession}
        />
        <TranscriptPanel
          activeSession={activeSession}
          messageForm={messageForm}
          sendingMessage={sendingMessage}
          recordingSupported={recordingSupported}
          isRecording={isRecording}
          speechStatus={speechStatus}
          speechPlaybackSupported={speechPlaybackSupported}
          onPlayAudio={playTranslatedAudio}
          onToggleRecording={handleToggleRecording}
          onDirectionChange={handleDirectionChange}
          onChange={handleMessageFieldChange}
          onSubmit={handleMessageSubmit}
        />
      </section>
    </main>
  );
}
