package nl.xservices.plugins;

import android.content.Context;

import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Locale;
import java.net.URISyntaxException;


public class LaunchMyApp extends CordovaPlugin {

  private static final String ACTION_CHECKINTENT = "checkIntent";
  private static final String ACTION_CLEARINTENT = "clearIntent";
  private static final String ACTION_GETLASTINTENT = "getLastIntent";
  public static final String CH_BEKBPAY_TWINTREGISTRATION = "TWINTREGISTRATION";
  private static final String OPEN_EXTERNAL_APP = "openExternalApp";


  private String lastIntentString = null;

  /**
   * We don't want to interfere with other plugins requiring the intent data,
   * but in case of a multi-page app your app may receive the same intent data
   * multiple times, that's why you'll get an option to reset it (null it).
   *
   * Add this to config.xml to enable that behaviour (default false):
   *   <preference name="CustomURLSchemePluginClearsAndroidIntent" value="true"/>
   */
  private boolean resetIntent;

  @Override
  public void initialize(final CordovaInterface cordova, CordovaWebView webView){
    this.resetIntent = preferences.getBoolean("resetIntent", false) ||
        preferences.getBoolean("CustomURLSchemePluginClearsAndroidIntent", false);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (ACTION_CHECKINTENT.equalsIgnoreCase(action)) {
      final Intent intent = ((CordovaActivity) this.webView.getContext()).getIntent();
      final String intentString = intent.getAction();
      final String token = intent.getStringExtra("token");
      if (intentString != null && intentString.contains(CH_BEKBPAY_TWINTREGISTRATION) && token != null) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "bekbapp://com.edorex.BEKBPay/twintregistration?token=" + token));
      } else {
        callbackContext.error("App was not started via the launchmyapp URL scheme. Ignoring this errorcallback is the best approach.");
      }
      return true;
    } else if (OPEN_EXTERNAL_APP.equalsIgnoreCase(action)) {
      openExternalApp (args, callbackContext);
    }
    return false;
  }


  @Override
  public void onNewIntent(Intent intent) {
    final String intentString = intent.getAction();
    final String token = intent.getStringExtra("token");
    if (CH_BEKBPAY_TWINTREGISTRATION.equals(intentString) && token != null) {
      webView.loadUrl("javascript:handleOpenURL('bekbapp://com.edorex.BEKBPay/twintregistration?token=" + token + "');");
    }
  }


  public void openExternalApp (final JSONArray args, final CallbackContext callbackContext) {
    try {
      Intent intent = Intent.parseUri("intent://registerResult/#Intent;scheme=ch.twint.payment;package=ch.twint.payment;end", 0);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Context context = this.cordova.getActivity().getApplicationContext();
      context.startActivity(intent);
      callbackContext.success("Opened");
    } catch (URISyntaxException e) {
      callbackContext.error("Not opened.");
    }
  }


  private static String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }


  // Taken from commons StringEscapeUtils
  private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote,
                                            boolean escapeForwardSlash) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (str == null) {
      return;
    }
    int sz;
    sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.write("\\u" + hex(ch));
      } else if (ch > 0xff) {
        out.write("\\u0" + hex(ch));
      } else if (ch > 0x7f) {
        out.write("\\u00" + hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.write('\\');
            out.write('b');
            break;
          case '\n':
            out.write('\\');
            out.write('n');
            break;
          case '\t':
            out.write('\\');
            out.write('t');
            break;
          case '\f':
            out.write('\\');
            out.write('f');
            break;
          case '\r':
            out.write('\\');
            out.write('r');
            break;
          default:
            if (ch > 0xf) {
              out.write("\\u00" + hex(ch));
            } else {
              out.write("\\u000" + hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            if (escapeSingleQuote) {
              out.write('\\');
            }
            out.write('\'');
            break;
          case '"':
            out.write('\\');
            out.write('"');
            break;
          case '\\':
            out.write('\\');
            out.write('\\');
            break;
          case '/':
            if (escapeForwardSlash) {
              out.write('\\');
            }
            out.write('/');
            break;
          default:
            out.write(ch);
            break;
        }
      }
    }
  }

}

