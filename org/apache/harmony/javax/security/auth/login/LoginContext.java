package org.apache.harmony.javax.security.auth.login;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import org.apache.harmony.javax.security.auth.AuthPermission;
import org.apache.harmony.javax.security.auth.Subject;
import org.apache.harmony.javax.security.auth.callback.Callback;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.harmony.javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import org.apache.harmony.javax.security.auth.spi.LoginModule;

public class LoginContext {
    private static final String DEFAULT_CALLBACK_HANDLER_PROPERTY = "auth.login.defaultCallbackHandler";
    private static final int OPTIONAL = 0;
    private static final int REQUIRED = 1;
    private static final int REQUISITE = 2;
    private static final int SUFFICIENT = 3;
    private CallbackHandler callbackHandler;
    private ClassLoader contextClassLoader;
    private boolean loggedIn;
    private Module[] modules;
    private Map<String, ?> sharedState;
    private Subject subject;
    private AccessControlContext userContext;
    private boolean userProvidedConfig;
    private boolean userProvidedSubject;

    /* renamed from: org.apache.harmony.javax.security.auth.login.LoginContext.1 */
    class C09261 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ CallbackHandler val$cbHandler;

        C09261(CallbackHandler callbackHandler) {
            this.val$cbHandler = callbackHandler;
        }

        public Void run() throws Exception {
            LoginContext.this.contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (LoginContext.this.contextClassLoader == null) {
                LoginContext.this.contextClassLoader = ClassLoader.getSystemClassLoader();
            }
            if (this.val$cbHandler == null) {
                String property = Security.getProperty(LoginContext.DEFAULT_CALLBACK_HANDLER_PROPERTY);
                if (!(property == null || property.length() == 0)) {
                    LoginContext.this.callbackHandler = (CallbackHandler) Class.forName(property, true, LoginContext.this.contextClassLoader).newInstance();
                }
            } else {
                LoginContext.this.callbackHandler = this.val$cbHandler;
            }
            return null;
        }
    }

    /* renamed from: org.apache.harmony.javax.security.auth.login.LoginContext.2 */
    class C09272 implements PrivilegedExceptionAction<Void> {
        C09272() {
        }

        public Void run() throws LoginException {
            LoginContext.this.loginImpl();
            return null;
        }
    }

    /* renamed from: org.apache.harmony.javax.security.auth.login.LoginContext.3 */
    class C09283 implements PrivilegedExceptionAction<Void> {
        C09283() {
        }

        public Void run() throws LoginException {
            LoginContext.this.logoutImpl();
            return null;
        }
    }

    private final class Module {
        AppConfigurationEntry entry;
        int flag;
        Class<?> klass;
        LoginModule module;

        Module(AppConfigurationEntry appConfigurationEntry) {
            this.entry = appConfigurationEntry;
            LoginModuleControlFlag controlFlag = appConfigurationEntry.getControlFlag();
            if (controlFlag == LoginModuleControlFlag.OPTIONAL) {
                this.flag = LoginContext.OPTIONAL;
            } else if (controlFlag == LoginModuleControlFlag.REQUISITE) {
                this.flag = LoginContext.REQUISITE;
            } else if (controlFlag == LoginModuleControlFlag.SUFFICIENT) {
                this.flag = LoginContext.SUFFICIENT;
            } else {
                this.flag = LoginContext.REQUIRED;
            }
        }

        int getFlag() {
            return this.flag;
        }

        void create(Subject subject, CallbackHandler callbackHandler, Map<String, ?> map) throws LoginException {
            String loginModuleName = this.entry.getLoginModuleName();
            if (this.klass == null) {
                try {
                    this.klass = Class.forName(loginModuleName, false, LoginContext.this.contextClassLoader);
                } catch (Throwable e) {
                    throw ((LoginException) new LoginException("auth.39 " + loginModuleName).initCause(e));
                }
            }
            if (this.module == null) {
                try {
                    this.module = (LoginModule) this.klass.newInstance();
                    this.module.initialize(subject, callbackHandler, map, this.entry.getOptions());
                } catch (Throwable e2) {
                    throw ((LoginException) new LoginException("auth.3A " + loginModuleName).initCause(e2));
                } catch (Throwable e22) {
                    throw ((LoginException) new LoginException("auth.3A" + loginModuleName).initCause(e22));
                }
            }
        }
    }

    private class ContextedCallbackHandler implements CallbackHandler {
        private final CallbackHandler hiddenHandlerRef;

        /* renamed from: org.apache.harmony.javax.security.auth.login.LoginContext.ContextedCallbackHandler.1 */
        class C09291 implements PrivilegedExceptionAction<Void> {
            final /* synthetic */ Callback[] val$callbacks;

            C09291(Callback[] callbackArr) {
                this.val$callbacks = callbackArr;
            }

            public Void run() throws IOException, UnsupportedCallbackException {
                ContextedCallbackHandler.this.hiddenHandlerRef.handle(this.val$callbacks);
                return null;
            }
        }

        ContextedCallbackHandler(CallbackHandler callbackHandler) {
            this.hiddenHandlerRef = callbackHandler;
        }

        public void handle(Callback[] callbackArr) throws IOException, UnsupportedCallbackException {
            try {
                AccessController.doPrivileged(new C09291(callbackArr), LoginContext.this.userContext);
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof UnsupportedCallbackException) {
                    throw ((UnsupportedCallbackException) e.getCause());
                }
                throw ((IOException) e.getCause());
            }
        }
    }

    public LoginContext(String str) throws LoginException {
        init(str, null, null, null);
    }

    public LoginContext(String str, CallbackHandler callbackHandler) throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("auth.34");
        }
        init(str, null, callbackHandler, null);
    }

    public LoginContext(String str, Subject subject) throws LoginException {
        if (subject == null) {
            throw new LoginException("auth.03");
        }
        init(str, subject, null, null);
    }

    public LoginContext(String str, Subject subject, CallbackHandler callbackHandler) throws LoginException {
        if (subject == null) {
            throw new LoginException("auth.03");
        } else if (callbackHandler == null) {
            throw new LoginException("auth.34");
        } else {
            init(str, subject, callbackHandler, null);
        }
    }

    public LoginContext(String str, Subject subject, CallbackHandler callbackHandler, Configuration configuration) throws LoginException {
        init(str, subject, callbackHandler, configuration);
    }

    private void init(String str, Subject subject, CallbackHandler callbackHandler, Configuration configuration) throws LoginException {
        int i = OPTIONAL;
        this.subject = subject;
        this.userProvidedSubject = subject != null;
        if (str == null) {
            throw new LoginException("auth.00");
        }
        if (configuration == null) {
            configuration = Configuration.getAccessibleConfiguration();
        } else {
            this.userProvidedConfig = true;
        }
        SecurityManager securityManager = System.getSecurityManager();
        if (!(securityManager == null || this.userProvidedConfig)) {
            securityManager.checkPermission(new AuthPermission("createLoginContext." + str));
        }
        AppConfigurationEntry[] appConfigurationEntry = configuration.getAppConfigurationEntry(str);
        if (appConfigurationEntry == null) {
            if (!(securityManager == null || this.userProvidedConfig)) {
                securityManager.checkPermission(new AuthPermission("createLoginContext.other"));
            }
            appConfigurationEntry = configuration.getAppConfigurationEntry("other");
            if (appConfigurationEntry == null) {
                throw new LoginException("auth.35 " + str);
            }
        }
        this.modules = new Module[appConfigurationEntry.length];
        while (i < this.modules.length) {
            this.modules[i] = new Module(appConfigurationEntry[i]);
            i += REQUIRED;
        }
        try {
            AccessController.doPrivileged(new C09261(callbackHandler));
            if (this.userProvidedConfig) {
                this.userContext = AccessController.getContext();
            } else if (this.callbackHandler != null) {
                this.userContext = AccessController.getContext();
                this.callbackHandler = new ContextedCallbackHandler(this.callbackHandler);
            }
        } catch (PrivilegedActionException e) {
            throw ((LoginException) new LoginException("auth.36").initCause(e.getCause()));
        }
    }

    public Subject getSubject() {
        if (this.userProvidedSubject || this.loggedIn) {
            return this.subject;
        }
        return null;
    }

    public void login() throws LoginException {
        PrivilegedExceptionAction c09272 = new C09272();
        try {
            if (this.userProvidedConfig) {
                AccessController.doPrivileged(c09272, this.userContext);
            } else {
                AccessController.doPrivileged(c09272);
            }
        } catch (PrivilegedActionException e) {
            throw ((LoginException) e.getException());
        }
    }

    private void loginImpl() throws LoginException {
        int i;
        Throwable th;
        boolean z;
        Module module;
        boolean z2;
        Module[] moduleArr;
        int i2 = OPTIONAL;
        if (this.subject == null) {
            this.subject = new Subject();
        }
        if (this.sharedState == null) {
            this.sharedState = new HashMap();
        }
        Throwable th2 = null;
        int[] iArr = new int[4];
        int[] iArr2 = new int[4];
        Module[] moduleArr2 = this.modules;
        int length = moduleArr2.length;
        for (i = OPTIONAL; i < length; i += REQUIRED) {
            Module module2 = moduleArr2[i];
            try {
                module2.create(this.subject, this.callbackHandler, this.sharedState);
                if (module2.module.login()) {
                    int flag = module2.getFlag();
                    iArr2[flag] = iArr2[flag] + REQUIRED;
                    flag = module2.getFlag();
                    iArr[flag] = iArr[flag] + REQUIRED;
                    if (module2.getFlag() == SUFFICIENT) {
                        break;
                    }
                } else {
                    continue;
                }
            } catch (Throwable th3) {
                th = th3;
                if (th2 != null) {
                    th = th2;
                }
                if (module2.klass == null) {
                    iArr2[REQUIRED] = iArr2[REQUIRED] + REQUIRED;
                    th2 = th;
                    break;
                }
                int flag2 = module2.getFlag();
                iArr2[flag2] = iArr2[flag2] + REQUIRED;
                if (module2.getFlag() == REQUISITE) {
                    th2 = th;
                    break;
                }
                th2 = th;
            }
        }
        if (iArr[REQUIRED] == iArr2[REQUIRED]) {
            if (iArr[REQUISITE] != iArr2[REQUISITE]) {
                z = true;
            } else if (iArr2[REQUIRED] != 0 || iArr2[REQUISITE] != 0) {
                z = OPTIONAL;
            } else if (!(iArr[OPTIONAL] == 0 && iArr[SUFFICIENT] == 0)) {
                z = OPTIONAL;
            }
            iArr = new int[]{OPTIONAL, OPTIONAL, OPTIONAL, OPTIONAL};
            if (z) {
                moduleArr2 = this.modules;
                length = moduleArr2.length;
                th = th2;
                for (i = OPTIONAL; i < length; i += REQUIRED) {
                    module = moduleArr2[i];
                    if (module.klass != null) {
                        int flag3 = module.getFlag();
                        iArr2[flag3] = iArr2[flag3] + REQUIRED;
                        try {
                            module.module.commit();
                            flag2 = module.getFlag();
                            iArr[flag2] = iArr[flag2] + REQUIRED;
                        } catch (Throwable th22) {
                            if (th == null) {
                                th = th22;
                            }
                        }
                    }
                }
            } else {
                th = th22;
            }
            if (iArr[REQUIRED] == iArr2[REQUIRED]) {
                if (iArr[REQUISITE] == iArr2[REQUISITE]) {
                    z2 = true;
                } else if (iArr2[REQUIRED] == 0 || iArr2[REQUISITE] != 0) {
                    z2 = OPTIONAL;
                } else if (!(iArr[OPTIONAL] == 0 && iArr[SUFFICIENT] == 0)) {
                    z2 = OPTIONAL;
                }
                if (z2) {
                    this.loggedIn = true;
                    return;
                }
                moduleArr = this.modules;
                i = moduleArr.length;
                while (i2 < i) {
                    try {
                        moduleArr[i2].module.abort();
                    } catch (Throwable th222) {
                        if (th == null) {
                            th = th222;
                        }
                    }
                    i2 += REQUIRED;
                }
                if ((th instanceof PrivilegedActionException) && th.getCause() != null) {
                    th = th.getCause();
                }
                if (th instanceof LoginException) {
                    throw ((LoginException) new LoginException("auth.37").initCause(th));
                }
                throw ((LoginException) th);
            }
            z2 = true;
            if (z2) {
                this.loggedIn = true;
                return;
            }
            moduleArr = this.modules;
            i = moduleArr.length;
            while (i2 < i) {
                moduleArr[i2].module.abort();
                i2 += REQUIRED;
            }
            th = th.getCause();
            if (th instanceof LoginException) {
                throw ((LoginException) new LoginException("auth.37").initCause(th));
            }
            throw ((LoginException) th);
        }
        z = true;
        iArr = new int[]{OPTIONAL, OPTIONAL, OPTIONAL, OPTIONAL};
        if (z) {
            th = th222;
        } else {
            moduleArr2 = this.modules;
            length = moduleArr2.length;
            th = th222;
            for (i = OPTIONAL; i < length; i += REQUIRED) {
                module = moduleArr2[i];
                if (module.klass != null) {
                    int flag32 = module.getFlag();
                    iArr2[flag32] = iArr2[flag32] + REQUIRED;
                    module.module.commit();
                    flag2 = module.getFlag();
                    iArr[flag2] = iArr[flag2] + REQUIRED;
                }
            }
        }
        if (iArr[REQUIRED] == iArr2[REQUIRED]) {
            if (iArr[REQUISITE] == iArr2[REQUISITE]) {
                if (iArr2[REQUIRED] == 0) {
                }
                z2 = OPTIONAL;
            } else {
                z2 = true;
            }
            if (z2) {
                moduleArr = this.modules;
                i = moduleArr.length;
                while (i2 < i) {
                    moduleArr[i2].module.abort();
                    i2 += REQUIRED;
                }
                th = th.getCause();
                if (th instanceof LoginException) {
                    throw ((LoginException) th);
                }
                throw ((LoginException) new LoginException("auth.37").initCause(th));
            }
            this.loggedIn = true;
            return;
        }
        z2 = true;
        if (z2) {
            this.loggedIn = true;
            return;
        }
        moduleArr = this.modules;
        i = moduleArr.length;
        while (i2 < i) {
            moduleArr[i2].module.abort();
            i2 += REQUIRED;
        }
        th = th.getCause();
        if (th instanceof LoginException) {
            throw ((LoginException) new LoginException("auth.37").initCause(th));
        }
        throw ((LoginException) th);
    }

    public void logout() throws LoginException {
        PrivilegedExceptionAction c09283 = new C09283();
        try {
            if (this.userProvidedConfig) {
                AccessController.doPrivileged(c09283, this.userContext);
            } else {
                AccessController.doPrivileged(c09283);
            }
        } catch (PrivilegedActionException e) {
            throw ((LoginException) e.getException());
        }
    }

    private void logoutImpl() throws LoginException {
        int i = OPTIONAL;
        if (this.subject == null) {
            throw new LoginException("auth.38");
        }
        this.loggedIn = false;
        Throwable th = null;
        Module[] moduleArr = this.modules;
        int length = moduleArr.length;
        for (int i2 = OPTIONAL; i2 < length; i2 += REQUIRED) {
            try {
                moduleArr[i2].module.logout();
                i += REQUIRED;
            } catch (Throwable th2) {
                if (th == null) {
                    th = th2;
                }
            }
        }
        if (th != null || i == 0) {
            Throwable th3;
            if (!(th instanceof PrivilegedActionException) || th.getCause() == null) {
                th3 = th;
            } else {
                th3 = th.getCause();
            }
            if (th3 instanceof LoginException) {
                throw ((LoginException) th3);
            }
            throw ((LoginException) new LoginException("auth.37").initCause(th3));
        }
    }
}
