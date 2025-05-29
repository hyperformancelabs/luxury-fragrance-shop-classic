document.addEventListener('DOMContentLoaded', function() {
    // Tạo HTML cho chatbot
    const chatbotHTML = `
        <div id="chatbot-container" class="chatbot-container">
            <div class="chatbot-header">
                <h5>Hỗ trợ trực tuyến</h5>
                <button id="chatbot-close" class="chatbot-close-btn">×</button>
            </div>
            <div id="chatbot-messages" class="chatbot-messages">
                <div class="chatbot-message bot">
                    Xin chào! Tôi là trợ lý ảo của APH Perfume. Tôi có thể giúp gì cho bạn?
                </div>
            </div>
            <div class="chatbot-input">
                <input type="text" id="chatbot-input-text" placeholder="Nhập tin nhắn...">
                <button id="chatbot-send">Gửi</button>
            </div>
        </div>
        <button id="chatbot-toggle" class="chatbot-toggle-btn">
            <i class="fas fa-comments"></i>
        </button>
    `;
    
    // Thêm chatbot vào body
    const chatbotDiv = document.createElement('div');
    chatbotDiv.innerHTML = chatbotHTML;
    document.body.appendChild(chatbotDiv);
    
    // Xử lý sự kiện
    const chatbotToggle = document.getElementById('chatbot-toggle');
    const chatbotContainer = document.getElementById('chatbot-container');
    const chatbotClose = document.getElementById('chatbot-close');
    const chatbotInput = document.getElementById('chatbot-input-text');
    const chatbotSend = document.getElementById('chatbot-send');
    const chatbotMessages = document.getElementById('chatbot-messages');
    
    // Kiểm tra trạng thái chatbot từ localStorage
    const chatbotOpen = localStorage.getItem('chatbotOpen') === 'true';
    if (chatbotOpen) {
        chatbotContainer.style.display = 'flex';
        chatbotToggle.style.display = 'none';
    }
    
    // Ẩn/hiện chatbot
    chatbotToggle.addEventListener('click', function() {
        chatbotContainer.style.display = 'flex';
        chatbotToggle.style.display = 'none';
        localStorage.setItem('chatbotOpen', 'true');
    });
    
    chatbotClose.addEventListener('click', function() {
        chatbotContainer.style.display = 'none';
        chatbotToggle.style.display = 'flex';
        localStorage.setItem('chatbotOpen', 'false');
    });
    
    // Tạo hiệu ứng đang nhập
    function showTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chatbot-typing bot';
        typingDiv.innerHTML = `
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
        `;
        chatbotMessages.appendChild(typingDiv);
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
        return typingDiv;
    }
    
    // Xử lý gửi tin nhắn
    function sendMessage() {
        const message = chatbotInput.value.trim();
        if (message) {
            // Thêm tin nhắn của người dùng
            const userMessageDiv = document.createElement('div');
            userMessageDiv.className = 'chatbot-message user';
            userMessageDiv.textContent = message;
            chatbotMessages.appendChild(userMessageDiv);
            
            // Xóa input
            chatbotInput.value = '';
            
            // Cuộn xuống dưới
            chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
            
            // Hiển thị hiệu ứng đang nhập
            const typingIndicator = showTypingIndicator();
            
            // Giả lập phản hồi từ bot (trong thực tế, bạn sẽ gọi API)
            setTimeout(function() {
                // Xóa hiệu ứng đang nhập
                typingIndicator.remove();
                
                // Tạo phản hồi dựa trên tin nhắn
                let botResponse = getBotResponse(message);
                
                const botMessageDiv = document.createElement('div');
                botMessageDiv.className = 'chatbot-message bot';
                botMessageDiv.textContent = botResponse;
                chatbotMessages.appendChild(botMessageDiv);
                
                // Cuộn xuống dưới
                chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
            }, 1500);
        }
    }
    
    // Xử lý phản hồi của bot
    function getBotResponse(message) {
        message = message.toLowerCase();
        
        if (message.includes('xin chào') || message.includes('hello') || message.includes('hi')) {
            return 'Xin chào! Tôi có thể giúp gì cho bạn?';
        } 
        else if (message.includes('giá') || message.includes('bao nhiêu')) {
            return 'Giá sản phẩm của chúng tôi dao động từ 500.000đ đến 5.000.000đ tùy thuộc vào thương hiệu và dung tích. Bạn có thể xem chi tiết giá trên trang sản phẩm.';
        }
        else if (message.includes('khuyến mãi') || message.includes('giảm giá') || message.includes('sale')) {
            return 'Hiện tại chúng tôi đang có chương trình giảm giá 30% cho tất cả sản phẩm mới. Bạn có thể xem thêm thông tin tại trang chủ hoặc mục Flash Deal.';
        }
        else if (message.includes('giao hàng') || message.includes('vận chuyển') || message.includes('ship')) {
            return 'Chúng tôi giao hàng toàn quốc. Phí vận chuyển từ 30.000đ tùy khu vực. Miễn phí vận chuyển cho đơn hàng từ 1.000.000đ.';
        }
        else if (message.includes('thanh toán') || message.includes('payment')) {
            return 'Chúng tôi hỗ trợ nhiều phương thức thanh toán: COD (thanh toán khi nhận hàng), chuyển khoản ngân hàng, thẻ tín dụng/ghi nợ, và ví điện tử như MoMo.';
        }
        else if (message.includes('đổi trả') || message.includes('hoàn tiền')) {
            return 'Chính sách đổi trả: Quý khách có thể đổi trả sản phẩm trong vòng 7 ngày kể từ ngày nhận hàng nếu sản phẩm còn nguyên vẹn, chưa qua sử dụng.';
        }
        else if (message.includes('liên hệ') || message.includes('hotline') || message.includes('số điện thoại')) {
            return 'Bạn có thể liên hệ với chúng tôi qua số điện thoại: 028 1234 5678 hoặc email: info@aphperfume.com';
        }
        else if (message.includes('cửa hàng') || message.includes('địa chỉ') || message.includes('showroom')) {
            return 'Cửa hàng chính của chúng tôi đặt tại: 123 Đường Nguyễn Văn Linh, Quận 7, TP. Hồ Chí Minh. Chúng tôi mở cửa từ 9h đến 21h hàng ngày.';
        }
        else if (message.includes('nước hoa nam') || message.includes('perfume for men')) {
            return 'Chúng tôi có nhiều dòng nước hoa nam từ các thương hiệu nổi tiếng như Dior, Chanel, Versace, Tom Ford... Bạn có thể xem chi tiết tại mục Nước hoa Nam trên trang web.';
        }
        else if (message.includes('nước hoa nữ') || message.includes('perfume for women')) {
            return 'Chúng tôi có đa dạng nước hoa nữ từ các thương hiệu cao cấp như Chanel, Dior, Gucci, YSL... Bạn có thể xem chi tiết tại mục Nước hoa Nữ trên trang web.';
        }
        else if (message.includes('cảm ơn') || message.includes('thank')) {
            return 'Không có gì! Rất vui khi được hỗ trợ bạn. Nếu có thắc mắc gì khác, đừng ngần ngại hỏi nhé!';
        }
        else {
            return 'Cảm ơn bạn đã liên hệ. Hiện tại tôi chưa có thông tin về vấn đề này. Bạn có thể liên hệ trực tiếp với nhân viên tư vấn qua số điện thoại 028 1234 5678 hoặc email info@aphperfume.com để được hỗ trợ tốt nhất.';
        }
    }
    
    chatbotSend.addEventListener('click', sendMessage);
    
    chatbotInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});
