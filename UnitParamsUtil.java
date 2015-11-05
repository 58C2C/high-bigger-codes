
/*
 *@author wumengda
 */
public class UnitParamsUtil{
  /*
	 * 将CMCS类别(手机)单元参数放到map中
	 */
	public Map<String,String> unitParamsToMapWithCMCS(Info info) throws UnsupportedEncodingException{
		if (info == null){
			return new HashMap<String,String>();
		}
		Map<String,String> map=new HashMap<String,String>();
		Map<String,String> map_5461=new HashMap<String,String>();
		//取得类别下所有cmcs单元参数，不含单元参数实例值（缓存时间为1800秒）
		Map<Integer, PropertyEntity> cateParaMap = CMCSEntityBLL.getPropertyByCateIDLocalIDMapInt(info.getCateID(), 0);
		//取出当前info实体的单元参数list
		List<ParaClass> paraClassList = info.getPara();
		if (paraClassList != null) {
			int size=paraClassList.size();
			for (int i = 0; i <size; i++) {
				ParaClass kv=paraClassList.get(i);
				int paramId=kv.getValue().getParameterID();
				//把5461的字符串拆开，将键值对单独放到map中
				if(paramId==5461){
					String dicstr=kv.getValue().getParameterValue();
					map_5461=divide_5461(dicstr);
				}else{
					PropertyEntity pe = cateParaMap.get(paramId);
					if (pe == null){
						continue;
					}
					String key=pe.getParameterName().toLowerCase();
					String value=kv.getValue().getParameterValue();
					//将取出的键值对，放到map中，比如("minprice",5000)
					map.put(key, URLDecoder.decode(value,"utf-8"));
				}
			}
			//如果key重复，以5461中的参数为准
			map.putAll(map_5461);
			return map;
		}
		return new HashMap<String,String>();
	}
	
	/*
	 * 非cmcs类别（非手机）单元参数放到map中
	 */
	public Map<String, String> unitParamsToMap(Info info) throws Throwable {
		if (info == null){
			return new HashMap<String,String>();
		}
		List<ParaClass> paraClassList = info.getPara();
		int cateid = info.getCateID();
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> map_5461 = new HashMap<String, String>();
		HashMap<Integer, UnitParameter> cateParaMap = UnitParameterBLL.GetParaMapByCateIDkeyInt(cateid);
		HashMap<String, String> paraControValuesMap = UnitParameterBLL.GetParaControlValues(cateid);
		int size = info.getPara().size();
		if (paraClassList != null) {
			for (int i = 0; i < size; i++) {
				ParaClass kv = paraClassList.get(i);
				int paramId = kv.getValue().getParameterID();
				// 把5461的字符串拆开，将键值对单独放到map中
				if (paramId == 5461) {
					String dicstr = kv.getValue().getParameterValue();
					map_5461=divide_5461(dicstr);
				} else {
					UnitParameter up = cateParaMap.get(paramId);
					if (up == null) {
						continue;
					}
					InfoPara ip = kv.getValue();
					if (ip != null) {
						String key = up.getParameterName().toLowerCase();
						String value = UnitParameterBLL.GetParaControlValue(key, ip.getParameterValue(),paraControValuesMap);
						map.put(key, URLDecoder.decode(value, "utf-8"));
					}
				}
			}
		}
		map.putAll(map_5461);
		return map;
	}
	
	public Map<String,String> divide_5461(String str) throws UnsupportedEncodingException{
		Map<String, String> map = new HashMap<String, String>();
		String[] kvarr = str.split("&amp;");
		for (String kvstr : kvarr) {
			String[] k_v = kvstr.split("=");
			map.put(k_v[0], URLDecoder.decode(k_v[1], "utf-8"));
		}
		return map;
	}
	/*
	 * 将当前info实体的单元参数转换为可供FE在页面使用的map，
	 * 比如key是minprice，vaule就是具体的价格。
	 * @author wumengda
	 */
	public Map<String,String> displayUnitParamsMap(Info info){
		if(info==null){
			return null;
		}
		Map<String,String> map=new HashMap<String,String>();
		try{
			//获取当前info类别的listname
			String listName = InfoUtility.getInfoCateListNameByUrl(info.getUrl());
			// 判断是否CMCS类别
			boolean isCMCS = isCmcsCate(listName);
			if (isCMCS && !isExcludeCmcsCate(listName)) {
				map = unitParamsToMapWithCMCS(info);
			} else {
				map=unitParamsToMap(info);
			}
			map.put("oldlevel", DisplayPara(info, "oldlevel"));
			System.out.println("======="+JsonHelper.toJSONString(map));
			return map;
		}catch(Throwable e){
			log.error("mothod=displayUnitParamsMap exception="+e.getMessage());
			return map;
		}
		
	}
	
}
