let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer
function init(userId) {
    peer = new Peer(userId, {

        port: 443,
        path: '/'
    })

    peer.on('open', () => {
        Android.onPeerConnected()

    })
    peer.on('connection', conn => {


        // peerjs bug prevents this from firing: https://github.com/peers/peerjs/issues/636
//        call.on('close', () => {
//            console.log("call close event");
//            handlePeerDisconnect();
//        });
        // this one works
        conn.on('close', () => {
            console.log("conn close event");
            handlePeerDisconnect();
        });
    });
//peer.on('close', () => {
//    peer.destroy();
//            //console.log("yes im here")
//            Android.endCallForced()
//        })
    listen()
}

function handlePeerDisconnect() {
  // manually close the peer connections
  for (let conns in peer.connections) {
    peer.connections[conns].forEach((conn, index, array) => {
      console.log(`closing ${conn.connectionId} peerConnection (${index + 1}/${array.length})`, conn.peerConnection);
      conn.peerConnection.close();

      // close it using peerjs methods
      if (conn.close)
        conn.close();


    });
  }
}

let localStream
function listen() {
    peer.on('call', (call) => {

        navigator.getUserMedia({
            audio: true, 
            video: true
        }, (stream) => {
            localVideo.srcObject = stream
            localStream = stream

            call.answer(stream)
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream

                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"

            })

        })
        
    })

}

function startCall(otherUserId) {
    navigator.getUserMedia({
        audio: true,
        video: true
    }, (stream) => {

        localVideo.srcObject = stream
        localStream = stream

        const call = peer.call(otherUserId, stream)
        call.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream

            remoteVideo.className = "primary-video"
            localVideo.className = "secondary-video"
        })


    })

}
function endCallOtherUser(){
 handlePeerDisconnect()
 peer.destroy()
}
function endCall(){
handlePeerDisconnect()
 peer.destroy()
}

function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
} 

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
} 