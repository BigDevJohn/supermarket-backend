package jv.supermarket.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

       Boolean existsByNameAndBrand(String name, String brand);

       boolean existsByIdAndAvailable(Long id, boolean available);

       Product findByIdAndAvailable(Long id, boolean available);

       Page<Product> findAllByAvailable(boolean available, Pageable pageable);

       Page<Product> findByName(String name, Pageable pageable);

       Page<Product> findByNameAndAvailable(String name, boolean available, Pageable pageable);

       Page<Product> findByBrand(String brand, Pageable pageable);

       Page<Product> findByBrandAndAvailable(String brand, boolean available, Pageable pageable);

       Product findByBrandAndName(String brand, String name);

       Product findByBrandAndNameAndAvailable(String brand, String name, boolean available);

       @Query("""
                     SELECT DISTINCT p
                     FROM Product p
                     JOIN p.categories c
                     WHERE c.name = :name
                            """)
       Page<Product> findByCategoryName(@Param("name") String name, Pageable pageable);

       @Query("""
                     SELECT DISTINCT p
                     FROM Product p
                     JOIN p.categories c
                     WHERE c.name = :name
                     AND p.available = :available
                            """)
       Page<Product> findByCategoryNameAndAvailable(
                     @Param("name") String name,
                     @Param("available") boolean available,
                     Pageable pageable);

       Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

       Page<Product> findByNameContainingIgnoreCaseAndAvailable(String name, boolean available, Pageable pageable);

       Page<Product> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

       Page<Product> findByBrandContainingIgnoreCaseAndAvailable(String brand, boolean available, Pageable pageable);

}
