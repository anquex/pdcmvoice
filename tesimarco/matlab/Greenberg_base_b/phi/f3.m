function p=f3(a,x)

% p = f3(a,x)
%
% compute the infinite product stopping when the terms converge to 1

l=length(x);
p=zeros(1,l);

for k=1:l
    do=true;
    i=1;
    %valore al passo precedente
    y0=f2(a,x(k),1);
    v0=1;
    p(k)=y0;
    while (do)
      v1=bitshift(1,i);
      %valore al passo attuale
      y1=f2(a,x(k),v1);
      if (y0==y1)
            % allora converge a 1
            do=false;
      else
          for ii=v0+1:v1
            p(k)=p(k).*f2(a,x(k),ii);
          end
          y0=y1;
          v0=v1;
      end
      i=i+1;
    end   
end
