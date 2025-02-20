require("dotenv").config();
const jwt = require("jsonwebtoken");

// Move sensitive data to environment variables
const API_KEY = "Ub1e970bQPCb3R5KWNtUKA";
const API_SECRET = "3Fk70oQdL03aVPunO5C3BSvZjL1mABeq";
const MEETING_NUMBER = "7712689913";
const ROLE = 0; // 0 for attendee, 1 for host

function generateZoomSignature() {
    const iat = Math.floor(Date.now() / 1000);
    const exp = iat + 60 * 60; // 1-hour expiration

    const payload = {
        sdkKey: API_KEY,
        mn: MEETING_NUMBER,
        role: ROLE,
        iat: iat,
        exp: exp,
        appKey: API_KEY,
        tokenExp: iat + 60 * 60 * 2, // 2-hour expiration
    };

    try {
        return jwt.sign(payload, API_SECRET, { algorithm: "HS256" });
    } catch (error) {
        console.error("Error generating signature:", error);
        throw error;
    }
}

// Function to initialize and join Zoom meeting
function initZoom() {
    // Create the required HTML element if it doesn't exist
    if (!document.getElementById('zmmtg-root')) {
        const zoomContainer = document.createElement('div');
        zoomContainer.id = 'zmmtg-root';
        document.body.appendChild(zoomContainer);
    }

    // Load Zoom SDK
    const ZoomMtg = window.ZoomMtg;
    ZoomMtg.setZoomJSLib('https://source.zoom.us/2.16.0/lib', '/av');
    ZoomMtg.preLoadWasm();
    ZoomMtg.prepareWebSDK();

    // Load language files
    ZoomMtg.i18n.load('en-US');
    ZoomMtg.i18n.reload('en-US');

    // Initialize Zoom
    ZoomMtg.init({
        leaveUrl: window.location.origin,
        success: (success) => {
            console.log('Zoom initialized successfully');

            // Join the meeting
            ZoomMtg.join({
                signature: generateZoomSignature(),
                sdkKey: API_KEY,
                meetingNumber: MEETING_NUMBER,
                userName: process.env.ZOOM_USER_NAME || 'Guest',
                userEmail: process.env.ZOOM_USER_EMAIL,
                passWord: process.env.ZOOM_MEETING_PASSWORD,
                success: (success) => {
                    console.log('Successfully joined the meeting');
                },
                error: (error) => {
                    console.error('Failed to join meeting:', error);
                }
            });
        },
        error: (error) => {
            console.error('Failed to initialize Zoom:', error);
        }
    });
}

// Add this to your HTML file
