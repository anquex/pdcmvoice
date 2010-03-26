%optimal probability for small sizes 
clc;
nmax=8;
Lopt=ones(nmax+1,1)*4*nmax;
Lopt(1)=1;
Lopt(2)=1;
Popt=ones(nmax+1,1);
for p=0.4:0.0001:0.5
    
    Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)

    for n=0:nmax
        for i=0:n
            Q(i+1,n+1)=binopdf(i,n,p);
        end
    end

    L=zeros(nmax+1,1);
    L(1)=1;
    L(2)=1;

    for n=2:nmax
        s=0;
        for i=1:n-1
            s=s+Q(i+1,n+1)*( L(i+1)+L(n-i+1) );
        end
        %for Basic BT
        %L(n+1)=(1+Q(0+1,n+1)+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
        %for MBT
        L(n+1)=(1+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
    end
    
    for i=3:nmax+1
        if(Lopt(i)>L(i))
            Lopt(i)=L(i);
            Popt(i)=p;
        end
    end
end

Lopt
Popt