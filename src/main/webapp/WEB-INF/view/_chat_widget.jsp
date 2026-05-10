<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- AI Chatbot Floating Widget -->
<div id="aiChatWidget" style="position: fixed; bottom: 20px; right: 20px; z-index: 9999;">
    <!-- Chat Button -->
    <button id="aiChatBtn" class="btn btn-primary rounded-circle shadow-lg" style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; transition: transform 0.2s;">
        <i class="bi bi-robot" style="font-size: 1.8rem;"></i>
    </button>

    <!-- Chat Window (Initially Hidden) -->
    <div id="aiChatWindow" class="card shadow-lg d-none" style="position: absolute; bottom: 80px; right: 0; width: 350px; height: 500px; border-radius: 12px; display: flex; flex-direction: column; border: none;">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center" style="border-radius: 12px 12px 0 0; padding: 12px 16px;">
            <span class="fw-bold"><i class="bi bi-robot me-2"></i>AI Asistan</span>
            <button type="button" class="btn-close btn-close-white" id="closeChatBtn" style="font-size: 0.8rem;"></button>
        </div>
        <div class="card-body" id="chatMessages" style="overflow-y: auto; flex: 1; padding: 16px; background: #f1f3f4; display: flex; flex-direction: column; gap: 10px;">
            <div class="ai-msg bg-white p-3 rounded shadow-sm" style="font-size: 0.9rem; border-left: 4px solid var(--accent); max-width: 90%;">
                Merhaba! Ben AI asistanınız. Yüklediğiniz PDF dosyaları hakkında bana soru sorabilirsiniz.
            </div>
        </div>
        <div class="card-footer bg-white p-3" style="border-radius: 0 0 12px 12px; border-top: 1px solid #eee;">
            <div class="input-group">
                <input type="text" id="chatInput" class="form-control" placeholder="Sorunuzu buraya yazın..." style="font-size: 0.9rem; border-radius: 20px 0 0 20px; border: 1px solid #ddd; padding: 8px 16px;">
                <button class="btn btn-primary" id="sendChatBtn" style="border-radius: 0 20px 20px 0; padding: 8px 16px;">
                    <i class="bi bi-send-fill"></i>
                </button>
            </div>
        </div>
    </div>
</div>

<style>
    #aiChatBtn:hover { transform: scale(1.1); }
    .user-msg {
        background: var(--accent) !important;
        color: white !important;
        align-self: flex-end;
        border-radius: 15px 15px 0 15px !important;
    }
    .ai-msg {
        align-self: flex-start;
        border-radius: 15px 15px 15px 0 !important;
    }
</style>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const chatBtn = document.getElementById('aiChatBtn');
        const chatWindow = document.getElementById('aiChatWindow');
        const closeBtn = document.getElementById('closeChatBtn');
        const sendBtn = document.getElementById('sendChatBtn');
        const chatInput = document.getElementById('chatInput');
        const chatMessages = document.getElementById('chatMessages');

        chatBtn.addEventListener('click', () => {
            chatWindow.classList.toggle('d-none');
            if (!chatWindow.classList.contains('d-none')) {
                chatInput.focus();
            }
        });

        closeBtn.addEventListener('click', () => {
            chatWindow.classList.add('d-none');
        });

        function appendMessage(text, isAi) {
            const div = document.createElement('div');
            div.className = isAi ? 'ai-msg bg-white p-3 rounded shadow-sm' : 'user-msg p-3 rounded shadow-sm';
            div.style.fontSize = '0.9rem';
            div.style.maxWidth = '90%';
            if (isAi) div.style.borderLeft = '4px solid var(--accent)';
            div.innerText = text;
            chatMessages.appendChild(div);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        async function sendMessage() {
            const question = chatInput.value.trim();
            if (!question) return;

            appendMessage(question, false);
            chatInput.value = '';

            // Loading state
            const loadingDiv = document.createElement('div');
            loadingDiv.className = 'ai-msg bg-white p-3 rounded shadow-sm';
            loadingDiv.style.fontSize = '0.9rem';
            loadingDiv.innerHTML = '<div class="spinner-border spinner-border-sm text-primary" role="status"></div>';
            chatMessages.appendChild(loadingDiv);
            chatMessages.scrollTop = chatMessages.scrollHeight;

            try {
                const response = await fetch('${pageContext.request.contextPath}/api/chat/ask', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'question=' + encodeURIComponent(question)
                });
                const text = await response.text();
                chatMessages.removeChild(loadingDiv);
                appendMessage(text, true);
            } catch (error) {
                console.error('Chat error:', error);
                chatMessages.removeChild(loadingDiv);
                appendMessage('Üzgünüm, bir hata oluştu. Lütfen tekrar deneyin.', true);
            }
        }

        sendBtn.addEventListener('click', sendMessage);
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendMessage();
        });
    });
</script>
