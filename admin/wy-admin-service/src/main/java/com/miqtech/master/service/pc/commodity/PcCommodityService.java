package com.miqtech.master.service.pc.commodity;

import com.miqtech.master.consts.pcCommodity.PcCommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.commodity.PcCommodityDao;
import com.miqtech.master.entity.pc.commodity.PcCommodity;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shilina
 */
@Service
public class PcCommodityService {

    @Autowired
    private PcCommodityDao pcCommodityDao;
    @Autowired
    private QueryDao queryDao;
    @Autowired
    private JedisConnectionFactory redisConnectionFactory;

    public PcCommodity save(PcCommodity pcCommodity){
        if(pcCommodity.getId()!=null){
            pcCommodity.setUpdateDate(new Date());
        }else{
            pcCommodity.setCreateDate(new Date());
        }
        return pcCommodityDao.save(pcCommodity);
    }

    public void save(List<PcCommodity> list){
         pcCommodityDao.save(list);
    }

    public PcCommodity findById(Long id){
        return pcCommodityDao.findByIdAndIsValid(id,true);
    }

	public List<PcCommodity> getTopList() {
		String sql = "select * from pc_commodity where is_top=1 and is_valid-1;";
		return queryDao.queryObject(sql, PcCommodity.class);
	}

    public PageVO list(Integer page,Integer pageSize,String name,Integer isSale){
        if(page==null||page<=0){
            page=1;
        }
        if(pageSize==null||pageSize<=0){
            pageSize= PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
        }
        StringBuilder sb=new StringBuilder();
        if(StringUtils.isNotBlank(name)){
            sb.append(" and name like '%"+name+"%'");
        }
        if(isSale!=null){
            sb.append(" and is_sale="+isSale);
        }

        String countSql="SELECT count(*) FROM pc_commodity WHERE is_valid = 1";
        countSql+=sb.toString();
        Number count=queryDao.query(countSql);
        if(count.intValue()>0){
            int start=(page-1)*pageSize;
			String sql = "SELECT pc.id,pc.img,pc.`name`,pc.num,sum(pce.num) exchangeCount,pc.is_top,pc.is_sale,if(pc.cash>0,pc.cash,null) cash,"
					+
                    "if(pc.chip>0,pc.chip,null) chip\n" +
                    "FROM\n" +
                    "\tpc_commodity pc\n" +
                    "LEFT JOIN pc_commodity_exchange pce ON pc.id = pce.commodity_id AND pce.is_valid = 1 and pce.`status` between 1 and 4\n" +
                    "WHERE\n" +
                    "\tpc.is_valid = 1 \n" +sb.toString()+
                    " GROUP BY pc.id\n" +
                    "ORDER BY is_top DESC,IFNULL(pc.update_date,pc.create_date) DESC limit "+start+","+pageSize;
            List<Map<String,Object>> list=queryDao.queryMap(sql);
            return new PageVO(page, list, count,pageSize);

        }
        return new PageVO();
    }

    /**
     *根据商品id获取商品兑换量
     */
    public int getSaleCount(Long commodityId){
        String sql="SELECT count(*) from pc_commodity_exchange where is_valid=1 and `status` BETWEEN 1 and 4 and commodity_id="+commodityId;
        Number count=queryDao.query(sql);
        if(count==null){
            return 0;
        }
        return count.intValue();
    }

    /**
     *根据商品id获取商品剩余量
     */
    public int getLeftCount(Long commodityId){
        PcCommodity pcCommodity=findById(commodityId);
        Integer num=pcCommodity.getNum();
        if(num==null){
            return PcCommodityConstant.PC_COMMODITY_COUNT_INFINITE;
        }else{
            return num-getSaleCount(commodityId);
        }
    }


    /**
     * 商品上架时将讲商品剩余量存到redis
     */
    public void setLeftCount(long commodityId){
        RedisAtomicInteger commodityCount = new RedisAtomicInteger(PcCommodityConstant.PC_COMMODITY_NUM + commodityId,
                redisConnectionFactory,getLeftCount(commodityId));
    }

    /**
     * 商品下架时将redis数值清空
     */
    public void clearLeftCount(long commodityId){
        RedisAtomicInteger commodityCount = new RedisAtomicInteger(PcCommodityConstant.PC_COMMODITY_NUM + commodityId, redisConnectionFactory);
        commodityCount.expireAt(new Date());
    }


}
