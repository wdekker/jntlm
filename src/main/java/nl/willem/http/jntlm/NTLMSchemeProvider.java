package nl.willem.http.jntlm;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import sun.net.www.protocol.http.NTLMAuthSequence;

class NTLMSchemeProvider implements AuthSchemeProvider, AuthSchemeFactory {

    public AuthScheme create(HttpContext context) {
        return new NTLMScheme(new SSONTLMEngine());
    }

    @Override
    public AuthScheme newInstance(HttpParams params) {
        return create(null);
    }

    private static class SSONTLMEngine implements NTLMEngine {

        private NTLMAuthSequence sequence;

        @Override
        public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
            try {
                newSequence(null);
                return sequence.getAuthHeader(null);
            } catch (Exception e) {
                throw new NTLMEngineException("Problem getting a type 1 message.", e);
            }
        }

        private void newSequence(String domain) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Constructor<?> constructor = NTLMAuthSequence.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            sequence = (NTLMAuthSequence) constructor.newInstance(null, null, domain);
        }

        @Override
        public String generateType3Msg(String username, String password, String domain, String workstation, String challenge) throws NTLMEngineException {
            try {
                return sequence.getAuthHeader(challenge);
            } catch (IOException e) {
                throw new NTLMEngineException("Problem getting new type 3 message.", e);
            }
        }

    }
}