package ftpcom.lyd.ftpandroidclient.chain;

import android.widget.EditText;

public class PasswordChain extends LoginChain {
    public PasswordChain(EditText editText) {
        super(editText);
    }

    @Override
    protected String errorStr() {
        return "密码不能为空";
    }

    @Override
    protected String OKStr() {
        return super.OKStr();
    }
}
