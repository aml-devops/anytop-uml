package com.bytebridges.anytop.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

	private static final String SECRET = "my-super-secret-key-my-super-secret-key";

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}

	public String generateToken(String username) {

		return Jwts.builder().subject(username).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 86400000)).signWith(getKey()).compact();
	}

	public String extractUsername(String token) {

		Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();

		return claims.getSubject();
	}

	public boolean isTokenValid(String token) {

		try {

			Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);

			return true;

		} catch (Exception e) {
			return false;
		}
	}
}