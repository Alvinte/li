package li.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.annotation.Aop;
import li.annotation.Trans;
import li.ioc.Ioc;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Aop包裹类
 * 
 * @author li (limw@w.cn)
 * @version 0.1.1 (2012-09-20)
 */
public class AopInterceptor {
	/**
	 * 内置的TransFilter
	 */
	private static final AopFilter TRANS_FILTER = new TransFilter();

	/**
	 * Aop包裹一个对象
	 */
	public static Object getInstance(Class<?> type) {
		// 构造这个类型所有方法的AopFilter的集合
		final Map<Method, List<AopFilter>> filtersMap = new HashMap<>();
		for (Method method : type.getDeclaredMethods()) {// 对每一个方法
			List<AopFilter> filters = new ArrayList<>();
			Aop aop = method.getAnnotation(Aop.class);
			for (int i = 0; null != aop && i < aop.value().length; i++) {// 如果有@Aop注解,对每一个@Aop.value()的值
				filters.add(Ioc.get(aop.value()[i]));
			}
			if (null != method.getAnnotation(Trans.class)) {// 如果有@Trans注解
				filters.add(TRANS_FILTER);
			}
			filtersMap.put(method, filters);
		}
		// 创建代理
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback(new MethodInterceptor() {// 设置callback,使用AopChain代理执行方法
			public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return new AopChain(target, method, args, filtersMap.get(method), proxy).doFilter().getResult();
			}// 使用AopChian代理执行这个方法并返回值
		});
		return enhancer.create();// 创建代理对象
	}
}