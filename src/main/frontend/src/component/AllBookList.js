import axios from 'axios';
import React, { useCallback, useEffect, useRef, useState } from 'react'
import { useInView } from 'react-intersection-observer';
import { Col, Container, Row } from 'react-bootstrap';
import Button from './common/Button';
import '../css/AllBookList.css'
import { Link } from 'react-router-dom';
import Parser from 'html-react-parser';
import DOMPurify from 'dompurify';

function AllBookList() {
  
  const [allBookList, setAllBookList] = useState([]);
  const [hasNextPage, setHasNextPage] = useState(true);
  const page = useRef(0);
  const [ref, inView] = useInView();


  const fetch = useCallback(async () => {
    try {
      const { data } = await axios.get(`/api/books?_limit=10&_page=${page.current}`);
      const modifiedData = data.map((book) => {
        const imgUrlArray = JSON.parse(book.previewImgList.replace(/\\/g, "")
        );
        return {
          ...book,
          previewImgList: imgUrlArray,
        };
      });
      setAllBookList((prevBook) => [...prevBook, ...modifiedData]);
      setHasNextPage(data.length === 10);

      if(data.length === 10) {
        page.current += 1;
      }
    } catch(err) {
      console.error(err);
    }
  }, []);

  useEffect(() => {
    if(inView && hasNextPage) {
      fetch();
      console.log(inView, hasNextPage);
    }
  }, [fetch, hasNextPage, inView]);
      
  return (
   <div className="container1">
     <div className="main1">
          <ul className='all-book-list'>
          <h1>전체 도서</h1>
            {allBookList.map((bookDetail, key) => {
              const standardPrice = parseInt(bookDetail.priceStandard);
              const salesPrice = parseInt(bookDetail.priceSales);

              const formattedStdPrice = standardPrice.toLocaleString();
              const formattedSalesPrice = salesPrice.toLocaleString();

              const fullDesc = bookDetail.fullDescription || "";

              return (
              <li className='book-list' key={key}>
                <div className='book-cover'>
                  <Link to={`/bookdetail/${bookDetail.isbn13}`}>
                  <img src={bookDetail.previewImgList[0]}
                   width="180px"
                   height="250px"
                   alt="book-cover" />
                   </Link>
                </div>
                <div className='info'>
                  <div className='book-title'>
                   <Link to={`/bookdetail/${bookDetail.isbn13}`}>{Parser(DOMPurify.sanitize(bookDetail.title))}</Link>
                  </div>
                  <div className='author-pub'>
                    {bookDetail.author} · {bookDetail.publisher} · {bookDetail.pubDate}
                  </div>
                  <div className='price'>
                    <span className='green'>10%</span> <span className='sale-price'>
                      {formattedSalesPrice}원</span> <del>{formattedStdPrice}원</del>
                  </div>
                  <div className="desc">
                      {Parser(DOMPurify.sanitize(fullDesc.slice(0,150)) +
                            (fullDesc.length > 150 ? "..." : "")
                          )}
                  </div>
                </div>
                <div className="cart-buy">
                  <div className="btn-cart">
                    <Button violet="true" fullWidth>장바구니 담기</Button>
                  </div>
                  <div className="btn-buy">
                    <Button green="true" fullWidth>구매하기</Button>
                  </div>
                </div>
              </li>
            )})}
            <li ref={ref}></li>
          </ul>
     </div>
   </div>
  )
}

export default AllBookList