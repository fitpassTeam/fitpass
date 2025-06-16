import React, { useEffect, useRef, useState } from 'react';

const Chat = ({ userId, receiverId, senderType }) => {
  const [message, setMessage] = useState('');
  const [chatLog, setChatLog] = useState([]);
  const ws = useRef(null);

  useEffect(() => {
    // WebSocket 연결
    ws.current = new WebSocket(`ws://localhost:8080/ws/chat?userId=${userId}`);

    ws.current.onopen = () => {
      console.log('WebSocket 연결됨');
    };

    ws.current.onmessage = (event) => {
      const receivedMsg = event.data;
      setChatLog((prev) => [...prev, { sender: '상대방', content: receivedMsg }]);
    };

    ws.current.onclose = () => {
      console.log('WebSocket 연결 종료');
    };

    return () => {
      ws.current.close();
    };
  }, [userId]);

  const sendMessage = () => {
    if (!message.trim()) return;

    const payload = {
      senderId: Id,
      receiverId: receiverId,
      message: message,
      senderType: senderType
    };

    ws.current.send(JSON.stringify(payload));
    setChatLog((prev) => [...prev, { sender: '나', content: message }]);
    setMessage('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault(); // 폼 전송 방지
      sendMessage();
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto bg-white rounded shadow">
      <h2 className="text-xl font-bold mb-2">1:1 채팅</h2>
      <div className="border h-64 overflow-y-scroll p-2 mb-2">
        {chatLog.map((msg, idx) => (
          <div key={idx} className="mb-1">
            <strong>{msg.sender}:</strong> {msg.content}
          </div>
        ))}
      </div>
      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={handleKeyDown}
        className="border px-2 py-1 w-full mb-2"
        placeholder="메시지를 입력하세요"
      />
      <button
        onClick={sendMessage}
        className="bg-blue-500 text-white px-4 py-1 rounded"
      >
        전송
      </button>
    </div>
  );
};

export default Chat;
