package backend.event_management_system.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int INCREMENTS_BY = 1;
    private LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        super();
        loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    public Integer load(String key){
                        return 0;
                    }
                });
    }

    public void removeUserFromLoginAttemptCache(String email){
        loginAttemptCache.invalidate(email);
    }

    public void addUserToLoginAttemptCache(String email){
        int attempts =0;
        try {
            attempts = loginAttemptCache.get(email) + INCREMENTS_BY ;
        } catch (ExecutionException e){
            e.printStackTrace();
        }
        loginAttemptCache.put(email, attempts);
    }

    public boolean hasExceededMaxNumberOfAttempts( String email ){
        try {
           return loginAttemptCache.get(email) >= MAX_ATTEMPTS;
        } catch (ExecutionException e){
            e.printStackTrace();
        }
        return false;
    }
}
