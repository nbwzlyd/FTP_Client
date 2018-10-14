package ftpcom.lyd.ftpandroidclient.chain;

import android.text.TextUtils;
import android.widget.EditText;

import ftpcom.lyd.ftpandroidclient.util.ToastUtil;


public abstract class LoginChain {
    public static final String IP_TYPE="IP";
    public static final String PORT_TYPE="PORT";
    public static final String ACCOUNT_TYPE="ACCOUNT";
    public static final String PASSWORD_TYPE="PASSWORD";

    private LoginChain next;
    private EditText editText;
    private boolean isOk;

    LoginChain(EditText editText){
        this.editText = editText;
    }

    private boolean isValidate(){
        return editText!=null && !TextUtils.isEmpty(editText.getText());
    }

    public LoginChain setNext(LoginChain chain){
        next = chain;
        return next;
    }

    public final boolean doWork(){
        if (isValidate()) {
            if (next != null) {
                next.doWork();
            }
            isOk=true;
        } else  {
            isOk=false;
            ToastUtil.showShortToastCenter(errorStr());
        }

        return isOk;
    }

    protected String errorStr(){
        return "";
    }

    protected String OKStr(){
        return "";
    }
}

