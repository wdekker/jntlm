package nl.willem.http.jntlm;

import static com.sun.jna.platform.win32.Sspi.*;
import static com.sun.jna.platform.win32.WinError.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Sspi.CredHandle;
import com.sun.jna.platform.win32.Sspi.CtxtHandle;
import com.sun.jna.platform.win32.Sspi.SecBufferDesc;
import com.sun.jna.platform.win32.Sspi.TimeStamp;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.IntByReference;

class NTLMSchemeProvider implements AuthSchemeProvider, AuthSchemeFactory {

    public AuthScheme create(HttpContext context) {
        return new NTLMScheme(new SSONTLMEngine());
    }

    @Override
    public AuthScheme newInstance(HttpParams params) {
        return create(null);
    }

    private static class SSONTLMEngine implements NTLMEngine {

        private CtxtHandle securityContext;

        @Override
        public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
            CredHandle credentials = acquireCredentialsHandle();
            try {
                byte[] token = nextToken(credentials, null);
                return Base64.encodeBase64String(token);
            } finally {
                dispose(credentials);
            }
        }

        @Override
        public String generateType3Msg(String username, String password, String domain, String workstation, String challenge) throws NTLMEngineException {
            byte[] token = Base64.decodeBase64(challenge);
            token = nextToken(null, token);
            return Base64.encodeBase64String(token);
        }

        private CredHandle acquireCredentialsHandle() {
            CredHandle handle = new CredHandle();
            int rc = Secur32.INSTANCE.AcquireCredentialsHandle(null, "NTLM", SECPKG_CRED_OUTBOUND, null, null, null, null, handle, new TimeStamp());
            if (SEC_E_OK != rc) {
                throw new Win32Exception(rc);
            }
            return handle;
        }

        public byte[] nextToken(CredHandle credentials, byte[] challenge) {
            IntByReference attr = new IntByReference();
            SecBufferDesc buffer = challenge != null ? new SecBufferDesc(SECBUFFER_TOKEN, challenge) : null;
            SecBufferDesc token = new SecBufferDesc(SECBUFFER_TOKEN, MAX_TOKEN_SIZE);

            CtxtHandle previousContext = securityContext;
            securityContext = new CtxtHandle();
            int rc = Secur32.INSTANCE.InitializeSecurityContext(credentials, previousContext, null, ISC_REQ_CONNECTION, 0, SECURITY_NATIVE_DREP, buffer, 0,
                    securityContext, token, attr, null);
            if (rc != SEC_I_CONTINUE_NEEDED && rc != SEC_E_OK) {
                throw new Win32Exception(rc);
            }
            return token.getBytes();
        }

        public void dispose(CredHandle credentials) {
            int rc = Secur32.INSTANCE.FreeCredentialsHandle(credentials);
            if (SEC_E_OK != rc) {
                throw new Win32Exception(rc);
            }
        }

    }
}