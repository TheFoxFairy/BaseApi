package org.foxfairy.base.api.core.module.complie;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 代码检查工具
 */
@Component
public class CodeChecker {
    private final List<Pattern> whitelistPackages;
    private final List<Pattern> blacklistPackages;

    public CodeChecker() {
        whitelistPackages = new ArrayList<>();
        blacklistPackages = new ArrayList<>();

        // 设置默认的黑名单
        addBlacklistPackage("java.io.*");
        addBlacklistPackage("java.nio.*");
    }

    /**
     * 生成对应源代码的唯一编码
     * @param sourceCode 源代码内容
     * @return 唯一编码
     */
    public String generateHash(String sourceCode) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sourceCode.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash: " + e.getMessage(), e);
        }
    }

    public void addBlacklistPackage(String packageName) {
        blacklistPackages.add(Pattern.compile(packageName));
    }

    /**
     * 用于检测源代码是否是安全的
     * @param code 源代码
     * @return 返回True或False
     */
    public boolean isCodeSafe(String code) {
        String[] lines = code.split("\n");

        for (String line : lines) {
            if (line.trim().startsWith("import")) {
                String importedPackage = line.trim().substring("import".length()).trim();
                if (isBlacklistedPackage(importedPackage)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBlacklistedPackage(String packageName) {
        for (Pattern blacklistPackage : blacklistPackages) {
            if (blacklistPackage.matcher(packageName).matches()) {
                return true;
            }
        }
        return false;
    }
}
