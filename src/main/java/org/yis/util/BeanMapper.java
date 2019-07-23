package org.yis.util;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import java.util.List;

/**
 * @author milu
 * @Description orika bean转换器
 * @createTime 2019年07月22日 14:00:00
 */
public class BeanMapper {
    private static MapperFacade mapper;

    public BeanMapper() {
    }

    public static <S, D> D map(S source, Class<D> destinationClass) {
        return mapper.map(source, destinationClass);
    }

    public static <S, D> D map(S source, Type<S> sourceType, Type<D> destinationType) {
        return mapper.map(source, sourceType, destinationType);
    }

    public static <S, D> List<D> mapList(Iterable<S> sourceList, Class<S> sourceClass, Class<D> destinationClass) {
        return mapper.mapAsList(sourceList, TypeFactory.valueOf(sourceClass), TypeFactory.valueOf(destinationClass));
    }

    public static <S, D> List<D> mapList(Iterable<S> sourceList, Type<S> sourceType, Type<D> destinationType) {
        return mapper.mapAsList(sourceList, sourceType, destinationType);
    }

    public static <S, D> D[] mapArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapper.mapAsArray(destination, source, destinationClass);
    }

    public static <S, D> D[] mapArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapper.mapAsArray(destination, source, sourceType, destinationType);
    }

    public static <E> Type<E> getType(Class<E> rawType) {
        return TypeFactory.valueOf(rawType);
    }

    static {
        MapperFactory mapperFactory = (new DefaultMapperFactory.Builder()).build();
        mapper = mapperFactory.getMapperFacade();
    }
}
