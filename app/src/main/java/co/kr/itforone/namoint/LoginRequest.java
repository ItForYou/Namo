package co.kr.itforone.namoint;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest
{
    private static final String URL = "http://namoint.itforone.co.kr/bbs/ajax.Android.loginchk.php";
    private Map<String, String> parameters = new HashMap();

    public LoginRequest(String id, String password, Response.Listener<String> listener)
    {
        super(Method.POST, URL, listener, null);
        this.parameters.put("mb_id", id);
        this.parameters.put("mb_password", password);
    }

    @Override
    public Map<String, String> getParams()
    {
        return this.parameters;
    }
}
