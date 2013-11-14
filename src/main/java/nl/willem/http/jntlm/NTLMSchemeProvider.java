package nl.willem.http.jntlm;

import static com.sun.jna.platform.win32.Sspi.SECBUFFER_TOKEN;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import waffle.util.Base64;
import waffle.windows.auth.IWindowsSecurityContext;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

import com.sun.jna.platform.win32.Sspi.SecBufferDesc;

class NTLMSchemeProvider implements AuthSchemeProvider, AuthSchemeFactory {

    public AuthScheme create(HttpContext context) {
        return new NTLMScheme(new SSONTLMEngine());
    }

    @Override
    public AuthScheme newInstance(HttpParams params) {
        return create(null);
    }

    private static class SSONTLMEngine implements NTLMEngine {

        private IWindowsSecurityContext securityContext;

        @Override
        public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
            securityContext = WindowsSecurityContextImpl.getCurrent("NTLM", null);
            return Base64.encode(securityContext.getToken());
        }

        @Override
        public String generateType3Msg(String username, String password, String domain, String workstation, String challenge) throws NTLMEngineException {
            SecBufferDesc buffer = new SecBufferDesc(SECBUFFER_TOKEN, Base64.decode(challenge));
            securityContext.initialize(securityContext.getHandle(), buffer, null);
            return Base64.encode(securityContext.getToken());
        }
        
    }
}