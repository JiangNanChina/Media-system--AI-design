package com.example.photography.service.impl;

import com.example.photography.dto.request.LoginRequest;
import com.example.photography.dto.request.RegisterRequest;
import com.example.photography.dto.response.LoginResponse;
import com.example.photography.dto.response.RegisterResponse;
import com.example.photography.model.entity.Department;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.service.AuthService;
import com.example.photography.service.DepartmentService;
import com.example.photography.service.EmailVerificationService;
import com.example.photography.service.UserDeviceService;
import com.example.photography.service.UserService;
import com.example.photography.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 閻犱降鍊涢惁澶愬嫉瀹ュ懎顫ら悗鍦仧楠炲洨鐚?
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Value("${app.admin.secret-key:PHOTOGRAPHY_ADMIN_2024}")
    private String adminSecretKey;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            // 闁哄被鍎叉竟姗€鎮介妸锕€鐓?
            User user = userService.findByUsernameOrEmail(request.getUsername());
            
            // 婵☆偀鍋撻柡灞诲劤閺併倝骞嬮柨瀣﹂柛姘剧畱閹酣鎮?
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new RuntimeException("??????");
            }
            
            // 濡ょ姴鐭侀惁澶屸偓闈涙閻?
            if (!verifyPassword(user, request.getPassword())) {
                throw new RuntimeException("闁活潿鍔嶉崺娑㈠触瀹ュ棗鐏楅悗闈涙閻栨粓鏌ㄥ▎鎺濆殩");
            }
            
            // 闁汇垻鍠愰崹娆絎T濞寸姰鍊楁晶?
            String token = jwtUtil.generateToken(
                user.getUsername(), 
                user.getRole().name(), 
                user.getId()
            );
            
            // 闁哄瀚紓鎾诲传瀹ュ懐瀹?
            return new LoginResponse(
                token,                                                          // String token
                user.getUsername(),                                             // String username
                user.getRealName(),                                             // String realName
                user.getEmail(),                                                // String email
                user.getRole(),                                                 // UserRole role
                user.getId(),                                                   // Long userId
                user.getDepartment() != null ? user.getDepartment().getName() : null, // String departmentName
                user.getAvatarUrl(),                                            // String avatarUrl
                user.getCreatedAt()                                             // LocalDateTime createdAt
            );
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("闁谎嗩嚙缂嶅秵寰勬潏顐バ? " + e.getMessage());
        }
    }
    
    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            // 闁稿繐鐗愮换妯兼偘鐏炵晫鍞ㄩ柡鍫墰濞堟垿鎮介妸锕€鐓曢柛姘Т閻︽垿鎯嶆笟鈧悰娆戞嫚?
            User user = userService.findByUsernameOrEmail(request.getUsername());
            
            if (user == null) {
                throw new RuntimeException("?????");
            }
            
            // 妫ｅ啯鏆?闂佹彃绉烽々锔锯偓鐟邦槸閸欏繑绌遍纰辨Щ闁挎稒纰嶉ˉ鍛村蓟閵壯勬殢闁规挳鏀卞Σ鎼佸触閿曚緡娼剁紒鍌欒兌閺?
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new RuntimeException("?????????????");
            }
            
            if (!verifyPassword(user, request.getPassword())) {
                throw new RuntimeException("Password incorrect");
            }
            
            // 閻犱焦鍎抽ˇ顒侇殽瀹€鍐闁挎稑鐗嗚ぐ褏鈧絻顫夐崹姘跺川濡灝顦╅柟瀵告焿缁绘鎮板畝鍐惧晭濠㈣泛娲ㄧ划锔锯偓瑙勭啲缁辨繄绮婚敍鍕€為柛娑欘焾婢跺嫰骞嬮柨瀣骏闂傚洠鍋撶紓浣瑰灥閻ｅ墽鎷嬮幆褜妲甸柨?
            if (user.getRole() == UserRole.MEMBER && request.getDeviceInfo() != null) {
                UserDeviceService.DeviceValidationResult deviceResult = 
                    userDeviceService.validateAndBindDevice(user, request.getDeviceInfo(), ipAddress, userAgent);
                
                if (!deviceResult.isSuccess()) {
                    throw new RuntimeException(deviceResult.getMessage());
                }
                
                // 閻犱焦婢樼紞宥囨媼閹屾У濡ょ姴鐭侀惁澶岀磼閹惧浜?
                if (deviceResult.getAction() == UserDeviceService.DeviceValidationResult.ValidationAction.FIRST_BIND) {
                    // 濡絾鐗楅鑲╃磼閹存繄鏆伴悹浣瑰劤椤︻剟鎯冮崟顓熸殢闁规潙鍤栫槐婵嬪矗椤栨瑤绨版繛锝堫嚙婵偤鎮х憴鍕殨濠㈣泛瀚幃濠囨焻閺勫繒甯?
                    // 婵絾鏌ㄩ々褔宕ｉ幋锔瑰亾娓氣偓閳ь剚姘ㄩ悡锟犳焽椤旂粯顐界紒?
                }
            }
            
            // 闁汇垻鍠愰崹娆絎T濞寸姰鍊楁晶?
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
            
            return new LoginResponse(
                token,                                                              // String token
                user.getUsername(),                                                 // String username
                user.getRealName(),                                                 // String realName
                user.getEmail(),                                                    // String email
                user.getRole(),                                                     // UserRole role
                user.getId(),                                                       // Long userId
                user.getDepartment() != null ? user.getDepartment().getName() : null, // String departmentName
                user.getAvatarUrl(),                                                // String avatarUrl
                user.getCreatedAt()                                                 // LocalDateTime createdAt
            );
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("闁谎嗩嚙缂嶅秵寰勬潏顐バ? " + e.getMessage());
        }
    }
    
    @Override
    public LoginResponse refreshToken(String token) {
        try {
            String newToken = jwtUtil.refreshToken(token);
            if (newToken == null) {
                throw new RuntimeException("??????");
            }
            
            String username = jwtUtil.getUsernameFromToken(newToken);
            User user = userService.findByUsername(username);
            
            // 妫ｅ啯鏆?闂佹彃绉烽々锔锯偓鐟邦槸閸欏繑绌遍纰辨Щ闁挎稒鑹鹃崺娑㈠棘妫颁焦濮㈤柣妤€鏈鍌毼涢埀顒勫蓟閵壯勬殢闁规挳顥撴慨鎼佸箑?
            if (user == null) {
                throw new RuntimeException("?????");
            }
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new RuntimeException("?????????????");
            }
            
            return new LoginResponse(
                newToken,                                                       // String token
                user.getUsername(),                                             // String username
                user.getRealName(),                                             // String realName
                user.getEmail(),                                                // String email
                user.getRole(),                                                 // UserRole role
                user.getId(),                                                   // Long userId
                user.getDepartment() != null ? user.getDepartment().getName() : null, // String departmentName
                user.getAvatarUrl(),                                            // String avatarUrl
                user.getCreatedAt()                                             // LocalDateTime createdAt
            );
        } catch (Exception e) {
            throw new RuntimeException("??????: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 妫ｅ啯鏆?闂佹彃绉烽々锔锯偓鐟邦槸閸欏繑绌遍纰辨Щ闁挎稒宀搁悰娆戞嫚娴ｉ攱濮㈤柣妤€鏈鍌涚▕閻旀椿娲ｆ俊顐熷亾闁哄被鍎抽弫銈夊箣妞嬪骸笑闁?
            User user = userService.findByUsername(username);
            if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
                return false; // 闁活潿鍔嶉崺娑欑▔瀹ュ懐鎽犻柛锔哄妽閸ㄣ劎鎮銈庢矗闁汇埄鐓夌槐婵囩閵堝洤顤傞柡鍐У閺?
            }
            
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LoginResponse getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("?????");
            }
            
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // 妫ｅ啯鏆?闂佹彃绉烽々锔锯偓鐟邦槸閸欏繑绌遍纰辨Щ闁挎稒淇洪獮蹇涘矗閺嵮呯Ъ闁告挸绉堕弫銈夊箣閾氬倷绻嗛柟顓у灡濡炲倸螞閳ь剟寮婚妷褎鏆忛柟鎾棑婵悂骞€?
            if (user == null) {
                throw new RuntimeException("?????");
            }
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new RuntimeException("Account disabled, please login again");
            }
            
            return new LoginResponse(
                null,                                                           // String token (濞戞挸绉风换鎴﹀炊閻庣劇ken)
                user.getUsername(),                                             // String username
                user.getRealName(),                                             // String realName
                user.getEmail(),                                                // String email
                user.getRole(),                                                 // UserRole role
                user.getId(),                                                   // Long userId
                user.getDepartment() != null ? user.getDepartment().getName() : null, // String departmentName
                user.getAvatarUrl(),                                            // String avatarUrl
                user.getCreatedAt()                                             // LocalDateTime createdAt
            );
        } catch (Exception e) {
            throw new RuntimeException("????????: " + e.getMessage());
        }
    }
    
    @Override
    public RegisterResponse register(RegisterRequest request) {
        try {
            // 濡ょ姴鐭侀惁澶屸偓闈涙閻栨粍绋夐埀顒勬嚊鐎涙ǚ鍋?
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("??????????");
            }
            
            // 婵☆偀鍋撻柡灞诲劤閺併倝骞嬪畡鐗堝€抽柡鍕靛灠閹礁顔忛幓鎺旀憼闁?
            if (userService.existsByUsername(request.getUsername())) {
                throw new RuntimeException("??????");
            }
            
            // 婵☆偀鍋撻柡灞诲劦閸嬫牜绮绘潏銊π﹂柛姘剧畱閸戯紕鈧稒锚濠€?
            if (userService.existsByEmail(request.getEmail())) {
                throw new RuntimeException("??????");
            }

            emailVerificationService.verifyRegisterCode(request.getEmail(), request.getEmailCode());
            
            // 濠碘€冲€归悘澶愬及椤栨粠鍚€闁荤偛妫楅幉鍐测枖閵娿儱鏂€闁挎稑鐭傞悰娆戞嫚娴ｅ摜妲曢梺?
            if (request.getRole() == UserRole.ADMIN) {
                if (!validateAdminSecretKey(request.getAdminSecretKey())) {
                    throw new RuntimeException("???????");
                }
            }
            
            // 闁告帗绋戠紓鎾诲棘閹殿喗鏆忛柟?
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRealName(request.getRealName());
            newUser.setEmail(request.getEmail());
            newUser.setRole(request.getRole());
            newUser.setEnabled(true); // 濮掓稒顭堥濠氬触椤栨粍鏆?
            
            // 閻犱礁澧介悿鍡涙焾閵娾晜锛?
            if (request.getDepartmentId() != null) {
                Department department = departmentService.findById(request.getDepartmentId());
                newUser.setDepartment(department);
            }
            
            // 濞ｅ洦绻傞悺銊╂偨閵婏箑鐓?
            User savedUser = userService.save(newUser);
            
            // 闁哄瀚紓鎾诲传瀹ュ懐瀹?
            String departmentName = savedUser.getDepartment() != null ? 
                savedUser.getDepartment().getName() : null;
            
            String message = request.getRole() == UserRole.ADMIN ?
                "?????????" : "??????";
            
            return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRealName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                departmentName,
                message
            );
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("????: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateAdminSecretKey(String secretKey) {
        return StringUtils.hasText(secretKey) && adminSecretKey.equals(secretKey);
    }

    private boolean verifyPassword(User user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        try {
            return passwordEncoder.matches(rawPassword, storedPassword);
        } catch (IllegalArgumentException ex) {
            // Compatibility for legacy deployments with plaintext passwords in DB.
            log.warn("Detected non-BCrypt password format for user: {}", user.getUsername());
            if (storedPassword.equals(rawPassword)) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                userService.save(user);
                log.info("Upgraded legacy plaintext password to BCrypt for user: {}", user.getUsername());
                return true;
            }
            return false;
        }
    }
}
