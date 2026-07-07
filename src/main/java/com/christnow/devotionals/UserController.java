package com.christnow.devotionals;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import org.springframework.web.bind.annotation.*;


import com.christnow.devotionals.models.User;
import com.christnow.devotionals.payload.LoginRequest;
import com.christnow.devotionals.repositories.UserRepository;
import com.christnow.devotionals.security.AuthUtils;
import com.christnow.devotionals.security.JwtUtil;
import com.christnow.devotionals.services.AdminService;
import com.christnow.devotionals.services.UserService;


@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "https://christnow.co",
        "https://www.christnow.co"
})
@RestController
@RequestMapping({"/users", "/api/users"})
public class UserController {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()
                    || user.getEmail() == null || user.getEmail().trim().isEmpty()
                    || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username, email, and password cannot be null or empty");
            }

            user.setUsername(user.getUsername().trim());
            user.setEmail(user.getEmail().trim().toLowerCase());

            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already taken");
            }


            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);


            String token = jwtUtil.generateToken(savedUser.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("email", savedUser.getEmail());
            response.put("token", token);
            response.put("message", "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user: " + e.getMessage());
        }
    }


    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail() == null ? "" : loginRequest.getEmail().trim().toLowerCase();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
            }


            User user = optionalUser.get();
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
            }


            String token = jwtUtil.generateToken(user.getEmail());


            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login error: " + e.getMessage());
        }
    }


    // --------- 3 Free Courses: Add & Get -----------
    @PostMapping("/{email}/free-courses/{courseId}")
    public ResponseEntity<String> addFreeCourse(
            @PathVariable String email,
            @PathVariable Long courseId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in required.");
        }
        String callerEmail = AuthUtils.resolveEmail(authentication);
        if (callerEmail == null || !callerEmail.equalsIgnoreCase(email.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only add free courses to your own account.");
        }
        try {
            userService.addFreeCourseToUser(callerEmail, courseId);
            return ResponseEntity.ok("Added");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }


    @GetMapping("/{email}/free-courses")
    public ResponseEntity<?> getUserFreeCourses(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user.getFreeCourses());
    }

    @GetMapping("/debug-auth-header")
    public Map<String, Object> debugAuthHeader(HttpServletRequest request) {
        Map<String, Object> out = new HashMap<>();
        String auth = request.getHeader("Authorization");
        out.put("authorizationHeader", auth);
        out.put("startsWithBearer", auth != null && auth.startsWith("Bearer "));
        out.put("length", auth == null ? 0 : auth.length());
        return out;
    }

    // ---------------- PROFILE (SAFE) ----------------
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication found");
        }


        String email = AuthUtils.resolveEmail(authentication);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not resolve signed-in email");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        // IMPORTANT: never include password or UserDetails in the response
        Map<String, Object> out = new HashMap<>();
        out.put("source", "UserController.getProfile_SAFE_v2");
        out.put("email", user.getEmail());
        out.put("username", user.getUsername());
        out.put("freeCourseIds", user.getFreeCourses() == null
                ? java.util.List.of()
                : user.getFreeCourses().stream().map(c -> c.getId()).toList());
        out.put("ownedCourseIds", user.getOwnedCourses() == null
                ? java.util.List.of()
                : user.getOwnedCourses().stream().map(c -> c.getId()).toList());
        out.put("admin", adminService.canManageCourses(user.getEmail()));
        out.put("isConfiguredAdmin", adminService.isAdmin(user.getEmail()));
        out.put("coursesEmpty", adminService.isCoursesEmpty());
        out.put("adminConfigured", adminService.isAdminConfigured());


        return ResponseEntity.ok(out);
    }
}
