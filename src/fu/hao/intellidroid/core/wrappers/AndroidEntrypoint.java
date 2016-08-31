package fu.hao.intellidroid.core.wrappers;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * Description: Methods that can be the start point of the execution.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/30
 */
public class AndroidEntrypoint extends DefaultEntrypoint {

    public AndroidEntrypoint(IMethod method, IClassHierarchy cha) {
        super(method, cha);
    }
}
