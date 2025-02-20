package src.main.java.com.example.saltbackend;

import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Base64;

@RestController
@RequestMapping("/api/zoom")
public class ZoomSignatureController {

    @Value("${zoom.sdk.key}")
    private String sdkKey;

    @Value("${zoom.sdk.secret}")
    private String sdkSecret;

    @PostMapping("/generate-signature")
    public ResponseEntity<?> generateSignature(@RequestBody ZoomRequest request) {
        try {
            String meetingNumber = request.getMeetingNumber();
            int role = request.getRole(); // 0 = Attendee, 1 = Host

            long ts = System.currentTimeMillis() - 30000;
            String payload = sdkKey + meetingNumber + ts + role;
            String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());

            String signature = Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT")
                    .setIssuer(sdkKey)
                    .setIssuedAt(new Date(ts))
                    .setExpiration(new Date(ts + 300000))
                    .claim("sdkKey", sdkKey)
                    .claim("mn", meetingNumber)
                    .claim("role", role)
                    .claim("iat", ts)
                    .claim("exp", ts + 300000)
                    .claim("appKey", sdkKey)
                    .claim("tokenExp", ts + 300000)
                    .signWith(SignatureAlgorithm.HS256, sdkSecret.getBytes())
                    .compact();

            return ResponseEntity.ok(new ZoomSignatureResponse(signature, sdkKey));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating signature: " + e.getMessage());
        }
    }
}

// DTO for Request
class ZoomRequest {
    private String meetingNumber;
    private int role;

    public String getMeetingNumber() { return meetingNumber; }
    public void setMeetingNumber(String meetingNumber) { this.meetingNumber = meetingNumber; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
}

// DTO for Response
class ZoomSignatureResponse {
    private String signature;
    private String apiKey;

    public ZoomSignatureResponse(String signature, String apiKey) {
        this.signature = signature;
        this.apiKey = apiKey;
    }

    public String getSignature() { return signature; }
    public String getApiKey() { return apiKey; }
}
