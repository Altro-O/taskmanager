package com.example.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.taskmanager.service.QRCodeService;

@Controller
public class QRController {
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @GetMapping("/qr/telegram")
    public ResponseEntity<byte[]> getTelegramQR(@RequestParam String email) {
        byte[] qrCode = qrCodeService.generateQRCodeForTelegram(email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
    }
} 